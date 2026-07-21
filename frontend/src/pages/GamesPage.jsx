import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import * as api from '../api/client';

function ReviewsSection({ gameId, canReview }) {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [form, setForm] = useState({ rating: '5', comment: '' });
  const [submitError, setSubmitError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  function loadReviews() {
    setLoading(true);
    setLoadError('');
    api
      .get(`/reviews?gameId=${gameId}`)
      .then(setReviews)
      .catch((err) => setLoadError(err.message))
      .finally(() => setLoading(false));
  }

  useEffect(loadReviews, [gameId]);

  function updateField(field) {
    return (event) => setForm((prev) => ({ ...prev, [field]: event.target.value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitError('');
    setSubmitting(true);
    try {
      await api.post('/reviews', { gameId, rating: Number(form.rating), comment: form.comment });
      setForm({ rating: '5', comment: '' });
      loadReviews();
    } catch (err) {
      setSubmitError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="reviews-section">
      {loading && <p>Loading reviews…</p>}
      {loadError && <p className="error">{loadError}</p>}

      {!loading && !loadError && (
        <ul className="reviews-list">
          {reviews.length === 0 && <li>No reviews yet.</li>}
          {reviews.map((review) => (
            <li key={review.id}>
              <strong>{review.rating}/5</strong>
              {review.comment && <span> — {review.comment}</span>}
            </li>
          ))}
        </ul>
      )}

      {canReview ? (
        <form onSubmit={handleSubmit} className="inline-form">
          <select value={form.rating} onChange={updateField('rating')}>
            <option value="1">1</option>
            <option value="2">2</option>
            <option value="3">3</option>
            <option value="4">4</option>
            <option value="5">5</option>
          </select>
          <input value={form.comment} onChange={updateField('comment')} placeholder="Comment" />
          <button type="submit" disabled={submitting}>
            {submitting ? 'Posting…' : 'Post review'}
          </button>
          {submitError && <p className="error">{submitError}</p>}
        </form>
      ) : (
        <p>Log in to leave a review.</p>
      )}
    </div>
  );
}

function GameListItem({ game, canEdit, onChanged }) {
  const [editing, setEditing] = useState(false);
  const [showReviews, setShowReviews] = useState(false);
  const [form, setForm] = useState({ title: game.title, genre: game.genre || '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  function updateField(field) {
    return (event) => setForm((prev) => ({ ...prev, [field]: event.target.value }));
  }

  async function handleSave(event) {
    event.preventDefault();
    setError('');
    setBusy(true);
    try {
      await api.put(`/games/${game.id}`, form);
      setEditing(false);
      onChanged();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleDelete() {
    if (!window.confirm(`Delete "${game.title}"?`)) {
      return;
    }
    setError('');
    setBusy(true);
    try {
      await api.del(`/games/${game.id}`);
      onChanged();
    } catch (err) {
      setError(err.message);
      setBusy(false);
    }
  }

  if (editing) {
    return (
      <li data-game-id={game.id}>
        <form onSubmit={handleSave} className="inline-form">
          <input value={form.title} onChange={updateField('title')} required />
          <input value={form.genre} onChange={updateField('genre')} placeholder="Genre" />
          <button type="submit" disabled={busy}>
            Save
          </button>
          <button type="button" onClick={() => setEditing(false)} disabled={busy}>
            Cancel
          </button>
        </form>
        {error && <p className="error">{error}</p>}
      </li>
    );
  }

  return (
    <li data-game-id={game.id}>
      <strong>{game.title}</strong>
      {game.genre && <span> — {game.genre}</span>}
      <span className="game-actions">
        {canEdit && (
          <>
            <button type="button" className="link-button" onClick={() => setEditing(true)}>
              Edit
            </button>
            <button type="button" className="link-button" onClick={handleDelete} disabled={busy}>
              Delete
            </button>
          </>
        )}
        <button type="button" className="link-button" onClick={() => setShowReviews((prev) => !prev)}>
          {showReviews ? 'Hide reviews' : 'Reviews'}
        </button>
      </span>
      {error && <p className="error">{error}</p>}
      {showReviews && <ReviewsSection gameId={game.id} canReview={canEdit} />}
    </li>
  );
}

function GamesPage() {
  const { user } = useAuth();
  const [games, setGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [form, setForm] = useState({ title: '', genre: '' });
  const [submitError, setSubmitError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  function loadGames() {
    setLoading(true);
    api
      .get('/games')
      .then(setGames)
      .catch((err) => setLoadError(err.message))
      .finally(() => setLoading(false));
  }

  useEffect(loadGames, []);

  function updateField(field) {
    return (event) => setForm((prev) => ({ ...prev, [field]: event.target.value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitError('');
    setSubmitting(true);
    try {
      await api.post('/games', form);
      setForm({ title: '', genre: '' });
      loadGames();
    } catch (err) {
      setSubmitError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section>
      <h1>Games</h1>

      {loading && <p>Loading…</p>}
      {loadError && <p className="error">{loadError}</p>}

      {!loading && !loadError && (
        <ul className="games-list">
          {games.length === 0 && <li>No games yet.</li>}
          {games.map((game) => (
            <GameListItem key={game.id} game={game} canEdit={Boolean(user)} onChanged={loadGames} />
          ))}
        </ul>
      )}

      {user ? (
        <>
          <h2>Add a game</h2>
          <form onSubmit={handleSubmit} className="form">
            <label htmlFor="title">Title</label>
            <input id="title" value={form.title} onChange={updateField('title')} required />

            <label htmlFor="genre">Genre</label>
            <input id="genre" value={form.genre} onChange={updateField('genre')} />

            {submitError && <p className="error">{submitError}</p>}

            <button type="submit" disabled={submitting}>
              {submitting ? 'Adding…' : 'Add game'}
            </button>
          </form>
        </>
      ) : (
        <p>Log in to add a game.</p>
      )}
    </section>
  );
}

export default GamesPage;

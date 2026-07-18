import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import * as api from '../api/client';

function GameListItem({ game, canEdit, onChanged }) {
  const [editing, setEditing] = useState(false);
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
      {canEdit && (
        <span className="game-actions">
          <button type="button" className="link-button" onClick={() => setEditing(true)}>
            Edit
          </button>
          <button type="button" className="link-button" onClick={handleDelete} disabled={busy}>
            Delete
          </button>
        </span>
      )}
      {error && <p className="error">{error}</p>}
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

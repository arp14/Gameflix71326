import { useEffect, useState } from 'react';
import * as api from '../api/client';

function ProfilePage() {
  const [profile, setProfile] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api
      .get('/me')
      .then(setProfile)
      .catch((err) => setError(err.message));
  }, []);

  if (error) {
    return <p className="error">{error}</p>;
  }

  if (!profile) {
    return <p>Loading…</p>;
  }

  return (
    <section>
      <h1>Profile</h1>
      <dl className="profile-details">
        <dt>Username</dt>
        <dd>{profile.username}</dd>
        <dt>Display name</dt>
        <dd>{profile.displayName}</dd>
        <dt>Email</dt>
        <dd>{profile.email}</dd>
        <dt>Member since</dt>
        <dd>{new Date(profile.createdAt).toLocaleDateString()}</dd>
      </dl>
    </section>
  );
}

export default ProfilePage;

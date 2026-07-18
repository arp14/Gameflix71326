import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function NavBar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/');
  }

  return (
    <nav className="nav-bar">
      <span className="brand">GameFlix</span>
      <NavLink to="/">Home</NavLink>
      <NavLink to="/games">Games</NavLink>
      {user ? (
        <>
          <span>Hi, {user.username}</span>
          <button type="button" className="link-button" onClick={handleLogout}>
            Log out
          </button>
        </>
      ) : (
        <>
          <NavLink to="/login">Log in</NavLink>
          <NavLink to="/register">Register</NavLink>
        </>
      )}
    </nav>
  );
}

export default NavBar;

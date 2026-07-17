import { NavLink } from 'react-router-dom';

function NavBar() {
  return (
    <nav className="nav-bar">
      <span className="brand">GameFlix</span>
      <NavLink to="/">Home</NavLink>
      <NavLink to="/games">Games</NavLink>
      <NavLink to="/login">Log in</NavLink>
      <NavLink to="/register">Register</NavLink>
    </nav>
  );
}

export default NavBar;

import React, { useState, FormEvent } from 'react';
import axios from 'axios';

const LogIn: React.FC = () => {
  const [username, setUsername] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [message, setMessage] = useState<string>('');

  const handleLogin = async (e: FormEvent) => {
    e.preventDefault();
    console.log(username, password)
    try {
      const response = await axios.post('http://localhost:8000/login', {
        username,
        password,
      });

      if (response.status === 200) {
        setMessage('Logged in successfully');
      } else {
        setMessage('Login failed');
      }
    } catch (err: any) {
      setMessage(`Error: ${err.response?.data?.message || err.message}`);
    }
  };

  return (
    <div>
      <h1>Login</h1>
      <form onSubmit={handleLogin}>
        <div>
          <label>Username:</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
        </div>
        <div>
          <label>Password:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>
        <button type="submit">Login</button>
      </form>
      {message && <p>{message}</p>}
    </div>
  );
};

export default LogIn;

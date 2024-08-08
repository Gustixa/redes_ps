import React, { useState, FormEvent, ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuthContext } from '@contexts/AuthContext';
import styles from './LogIn.module.css';

import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import OutlinedInput from '@mui/material/OutlinedInput';
import InputLabel from '@mui/material/InputLabel';
import InputAdornment from '@mui/material/InputAdornment';
import FormControl from '@mui/material/FormControl';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';

const LogIn: React.FC = () => {
  const [showPassword, setShowPassword] = useState(false);
  const [username, setUsername] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [message, setMessage] = useState<string>('');
  const handleMouseDownPassword = (event: React.MouseEvent<HTMLButtonElement>) => {
    event.preventDefault();
  };
  const { setAuthUser, setIsLoggedIn } = useAuthContext();
  const navigate = useNavigate();

  /**
   * Metodo para poder mostrar o no la contraseña
   * @returns 
   */
  const handleClickShowPassword = () => setShowPassword((show) => !show);

  /**
   * Metodo para obtener el valor del campo contraseña
   * @param e 
   */
  const handleChangePassword = (e: ChangeEvent<HTMLInputElement>) => {
    setPassword(e.target.value);
  };

  /**
   * Metodo para validar el usuario y pasar a la siguiente pantalla
   * @param e 
   */
  const handleLogin = async (e: FormEvent) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:8000/login', { username, password });
      if (response.status === 200) {
        setIsLoggedIn(true);
        setAuthUser({ correo: username });
        navigate('/');
      } else {
        setMessage('Login failed');
      }
    } catch (err: any) {
      setMessage(`Error: ${err.response?.data?.message || err.message}`);
    }
  };

  const handleRegistrarse = () => {
    navigate('/signup')
  }
  return (
    <div className={styles.box}>
      <h1 className={styles.title}>Login</h1>
      <form onSubmit={handleLogin}>
        <div className={styles.inputs}>
          <TextField 
            id="user-input" 
            label="Usuario" 
            variant="outlined"
            value={username}
            onChange={(e) => setUsername(e.target.value)} 
            fullWidth
          />
          <FormControl variant="outlined" fullWidth>
            <InputLabel htmlFor="outlined-adornment-password">Contraseña</InputLabel>
            <OutlinedInput
              id="outlined-adornment-password"
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={handleChangePassword}
              endAdornment={
                <InputAdornment position="end">
                  <IconButton
                    aria-label="toggle password visibility"
                    onClick={handleClickShowPassword}
                    onMouseDown={handleMouseDownPassword}
                    edge="end"
                  >
                    {showPassword ? <VisibilityOff /> : <Visibility />}
                  </IconButton>
                </InputAdornment>
              }
              label="Contraseña"
            />
          </FormControl>
        </div>
        <div className={styles.buttons}>
          <Button 
            variant="contained" 
            size="large" 
            type="submit">
            Iniciar Sesion
          </Button>
          <Button 
            variant="contained" 
            size="large"
            onClick={handleRegistrarse}
            >
            Registrarse
          </Button>
        </div>
      </form>
      {message && <p>{message}</p>}
    </div>
  );
};

export default LogIn;

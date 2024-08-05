import { Navigate, Outlet } from 'react-router-dom'
import './App.css'
import ProtectedRoute from '@routing/ProtectedRoute'

function App() {
  const isUserValid = false


  return <div>{isUserValid ? <Outlet/> : <Navigate to={"/login"}/>}</div>
}

export default App

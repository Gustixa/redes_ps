import { AuthProvider } from '@contexts/AuthContext'
import './App.css'
import Routing from '@routing/routing'

function App() {
  return (
    <AuthProvider>
      <Routing/>
    </AuthProvider>
  ) 
}

export default App

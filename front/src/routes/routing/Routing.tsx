import React from 'react'
import { Route, BrowserRouter, Routes } from 'react-router-dom'
import Home from "@pages/home"
import ProtectedRoute from '@routing/protectedRoute'
import LogIn from '@pages/logIn'
import SignUp from '@pages/signUp'


const Routing = () => {
    return (
        <BrowserRouter>
        {/** El protected route es global, pues no se desea tener acceso, a menos que haya iniciado sesion */}
            <Routes>
                <Route path='/login' element={<LogIn/>}></Route>
                <Route path='/signup' element={<SignUp/>}></Route>
                <Route
                    path='/'
                    element={(
                        <ProtectedRoute>
                            <Home/>
                        </ProtectedRoute>
                    )}
                >
                </Route>
            </Routes>
        </BrowserRouter>
    )
}

export default Routing
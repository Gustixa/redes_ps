import LogIn from "@pages/logIn"
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom"
import App from "../App"
import Home from "@pages/home"

const ProtectedRoute: React.FC = () => {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<App/>}>
                </Route>
                <Route path="/home" element={<Home/>}/>

                <Route path="/login" element={<LogIn/>}></Route>
                <Route path="*" element={<Navigate to={"/login"}/>}/>
            </Routes>
        </BrowserRouter>
    )
}

export default ProtectedRoute
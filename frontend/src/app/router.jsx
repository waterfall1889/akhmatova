import { createBrowserRouter, Navigate } from 'react-router-dom'
import { HomePage } from '../pages/HomePage/HomePage.jsx'
import { LoginPage } from '../pages/LoginPage/LoginPage.jsx'
import { RegistrationPage } from '../pages/RegistrationPage/RegistrationPage.jsx'
import { SignUpPage } from '../pages/SignUpPage/SignUpPage.jsx'

export function createAppRouter() {
  return createBrowserRouter([
    { path: '/', element: <HomePage /> },
    { path: '/login', element: <LoginPage /> },
    { path: '/register', element: <RegistrationPage /> },
    { path: '/register/success', element: <Navigate to="/register" replace /> },
    { path: '/signIn', element: <SignUpPage /> },
    { path: '*', element: <Navigate to="/" replace /> },
  ])
}

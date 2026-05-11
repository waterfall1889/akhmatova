import { createBrowserRouter, Navigate } from 'react-router-dom'
import { HomePage, MyHomeSection } from '../pages/HomePage/HomePage.jsx'
import { NovelEditorPage } from '../pages/HomePage/Components/NovelEditorPage.jsx'
import { NovelWorkshopSection } from '../pages/HomePage/Components/NovelWorkshopSection.jsx'
import { LoginPage } from '../pages/LoginPage/LoginPage.jsx'
import { RegistrationPage } from '../pages/RegistrationPage/RegistrationPage.jsx'
import { SignUpPage } from '../pages/SignUpPage/SignUpPage.jsx'

export function createAppRouter() {
  return createBrowserRouter([
    {
      path: '/',
      element: <HomePage />,
      children: [
        { index: true, element: <Navigate to="/auth" replace /> },
        { path: 'auth', element: <MyHomeSection /> },
        { path: 'novel-workshop', element: <NovelWorkshopSection /> },
        { path: 'novel-workshop/:storyId/edit', element: <NovelEditorPage /> },
      ],
    },
    { path: '/login', element: <LoginPage /> },
    { path: '/register', element: <RegistrationPage /> },
    { path: '/register/success', element: <Navigate to="/register" replace /> },
    { path: '/signIn', element: <SignUpPage /> },
    { path: '*', element: <Navigate to="/auth" replace /> },
  ])
}

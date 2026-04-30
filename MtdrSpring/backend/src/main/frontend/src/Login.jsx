import React from "react";
import { useNavigate } from 'react-router-dom';

function Login() {
  const navigate = useNavigate();

  return (
    <section className="min-h-screen flex">

      <div className="w-full md:w-1/2 flex flex-col justify-center px-10 lg:px-20 bg-white">
        <h1 className="text-4xl font-semibold text-gray-900">
          Bienvenido
        </h1>
        <h2 className="text-4xl text-red-500 italic mb-4">
          de vuelta
        </h2>

        <p className="text-gray-500 mb-8">Elige tu modo de acceso</p>

        <div className="space-y-4">
          <button onClick={() => navigate('/app')} className="w-full inline-flex items-center justify-center rounded-lg bg-blue-600 px-4 py-3 text-lg font-medium text-white hover:bg-blue-700">Entrar como Developer (App)</button>
          <button onClick={() => navigate('/manager')} className="w-full inline-flex items-center justify-center rounded-lg bg-green-600 px-4 py-3 text-lg font-medium text-white hover:bg-green-700">Entrar como Manager</button>
        </div>

      </div>

      <div className="hidden md:block md:w-1/2">
        <img
          src="https://dummyimage.com/720x600"
          alt="login"
          className="w-full h-full object-cover"
        />
      </div>
    </section>
  );
}

export default Login;
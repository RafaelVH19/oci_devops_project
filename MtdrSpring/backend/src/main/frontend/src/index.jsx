/*
## MyToDoReact version 1.0.
##
## Copyright (c) 2021 Oracle, Inc.
## Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
*/
/*
 * @author  jean.de.lavarene@oracle.com
 */

import React, { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import './index.css';
import App from './App';
import Landing from './Landing';
import Login from './Login';
import Dashboard from './Dashboard';
import RequireAuth from './components/RequireAuth';
import RequireManager from './components/RequireManager';
import LumiAssistant from './components/dashboard/LumiAssistant';
import DemoBanner from './components/DemoBanner';
import { isDemoMode } from './config/demoMode';
import { installDemoFetch } from './demo/installDemoFetch';

if (isDemoMode) {
  installDemoFetch();
  document.documentElement.classList.add('demo-mode-active');
}

createRoot(document.getElementById('root')).render(
  <StrictMode>
    {isDemoMode && <DemoBanner />}
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/landing" element={<Navigate to="/" replace />} />
        <Route path="/login" element={<Login />} />
        <Route
          path="/app"
          element={
            <RequireAuth>
              <App />
            </RequireAuth>
          }
        />
        <Route
          path="/dashboard/*"
          element={
            <RequireAuth>
              <RequireManager>
                <Dashboard />
              </RequireManager>
            </RequireAuth>
          }
        />
        <Route
          path="/lumi"
          element={
            <RequireAuth>
              <LumiAssistant />
            </RequireAuth>
          }
        />
        <Route path="/manager" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  </StrictMode>
);
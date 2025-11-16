import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import Login from './components/Login';
import Cadastro from './components/Cadastro';
import EsqueciSenha from './components/EsqueciSenha';
import ResetSenha from './components/ResetSenha';
import Home from "./components/Home";
import NotFoundPage from "./components/NotFoundPage";
import Professores from "./components/Professores";
import Matrizes from "./components/Matrizes";
import Turmas from "./components/Turmas";
import Disciplinas from "./components/Disciplinas";
import Configuracoes from "./components/Configuracoes";
import DetalhesProfessores from "./components/DetalhesProfessores";
import Relatorios from "./components/Relatorios";


function App() {
  return (
    <Router>
      <Routes>
        {/* Redireciona a raiz para o login */}
        <Route path="/" element={<Navigate to="/login" replace />} />
        
        <Route path="/login" element={<Login />} />
        <Route path="/cadastro" element={<Cadastro />} />
        <Route path="/esqueci-senha" element={<EsqueciSenha />} />
        <Route path="/reset-senha" element={<ResetSenha />} />

        {/* Tela principal após login */}
        <Route path="/home" element={<Home />} />

        {/* Página 404 */}
        <Route path="*" element={<NotFoundPage />} />

        {/* Redireciona pras respectivas páginas */}
        <Route path="/professores" element={<Professores />} />
        <Route path="/matrizes" element={<Matrizes />} />
        <Route path="/turmas" element={<Turmas />} />
        <Route path="/disciplinas" element={<Disciplinas />} />
        <Route path="/configuracoes" element={<Configuracoes />} />
        <Route path="/professores/:id" element={<DetalhesProfessores />} />
        <Route path="/relatorios" element={<Relatorios />} />

      </Routes>
    </Router>
  );
}

export default App;

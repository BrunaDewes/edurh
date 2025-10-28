import React, { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { API_BASE_URL } from '../config';
import "../styles/Login.css"; // reaproveitando estilo
import { useNavigate } from 'react-router-dom';

const ResetSenha = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token"); // token virá no link enviado por email
  const [novaSenha, setNovaSenha] = useState('');
  const [confirmarSenha, setConfirmarSenha] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const validarSenha = (senha) => {
    const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,}$/;
    return regex.test(senha);
  };

  const handleReset = async (e) => {
    e.preventDefault();
    setMessage('');

    if (novaSenha !== confirmarSenha) {
      setMessage("As senhas não coincidem.");
      return;
    }

    if (!validarSenha(novaSenha)) {
      setMessage("A senha deve ter no mínimo 8 caracteres, incluindo 1 maiúscula, 1 minúscula e 1 caractere especial.");
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/auth/reset-senha`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, novaSenha }),
      });

      if (response.ok) {
        setMessage("Senha redefinida com sucesso! Redirecionando para login...");
        setTimeout(() => navigate("/login"), 2000);
      } 
      else {
        const errorText = await response.text();
        setMessage(`Erro: ${errorText}`);
      }
    } catch (error) {
      setMessage("Erro de conexão. Verifique o backend.");
      console.error("Erro:", error);
    }
  };

  return (
    <div className="login-page">
      <div className="login-box">
        <h2 className="login-title">Redefinir Senha</h2>
        <form onSubmit={handleReset}>
          <div className="form-group">
            <label>Nova senha</label>
            <input
              type="password"
              value={novaSenha}
              onChange={(e) => setNovaSenha(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label>Confirmar nova senha</label>
            <input
              type="password"
              value={confirmarSenha}
              onChange={(e) => setConfirmarSenha(e.target.value)}
              required
            />
          </div>
          <button type="submit">Redefinir Senha</button>
        </form>
        {message && <p className="message">{message}</p>}
      </div>
    </div>
  );
};

export default ResetSenha;

import React, { useState } from 'react';
import { API_BASE_URL } from '../config';
import "../styles/Login.css"; // Reaproveitando o estilo do login

const EsqueciSenha = () => {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');

  const handleRecuperar = async (e) => {
    e.preventDefault();
    setMessage('');

    try {
      const response = await fetch(`${API_BASE_URL}/auth/esqueci-senha`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email }),
      });

      if (response.ok) {
        setMessage("Se esse email estiver cadastrado, um link de recuperação foi enviado.");
      } else {
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
        <h2 className="login-title">Recuperar Senha</h2>
        <form onSubmit={handleRecuperar}>
          <div className="form-group">
            <label>Email cadastrado</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          <button type="submit">Enviar link</button>
        </form>
        {message && <p className="message">{message}</p>}
      </div>
    </div>
  );
};

export default EsqueciSenha;

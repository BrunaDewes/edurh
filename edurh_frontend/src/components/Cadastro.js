import React, { useState } from 'react';
import { API_BASE_URL } from '../config';
import "../styles/Login.css"; // reutilizando estilo

const Cadastro = () => {
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [message, setMessage] = useState('');

  const validarSenha = (senha) => {
    const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*_]).{8,}$/;
    return regex.test(senha);
  };

  const handleCadastro = async (e) => {
    e.preventDefault();
    setMessage('');

    if (!validarSenha(senha)) {
      setMessage("A senha deve ter no mínimo 8 caracteres, incluindo 1 maiúscula, 1 minúscula e 1 caractere especial.");
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/usuarios`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nome, email, senha }),
      });

      if (response.ok) {
        setMessage("Cadastro realizado com sucesso! Você já pode fazer login.");
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
        <h2 className="login-title">Crie sua conta</h2>
        <form onSubmit={handleCadastro}>
          <div className="form-group">
            <label>Nome</label>
            <input
              type="text"
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label>Senha</label>
            <input
                type="password"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                required
            />
          </div>

          <small className="password-rules">
              A senha deve conter:
              <ul>
                <li>No mínimo 8 caracteres</li>
                <li>Pelo menos 1 letra maiúscula</li>
                <li>Pelo menos 1 letra minúscula</li>
                <li>Pelo menos 1 caractere especial (!@#$%^_&*)</li>
              </ul>
          </small>
          
          <button type="submit">Cadastrar</button>
        </form>
        {message && <p className="message">{message}</p>}
      </div>
    </div>
  );
};

export default Cadastro;

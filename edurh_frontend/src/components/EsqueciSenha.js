import React, { useState } from "react";
import { API_BASE_URL } from "../config";
import "../styles/Login.css"; // Reaproveitando o layout do login
import { Link } from "react-router-dom";

export default function EsqueciSenha() {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage("");
    setLoading(true);

    try {
      const res = await fetch(`${API_BASE_URL}/auth/esqueci-senha`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email }),
      });

      const text = await res.text(); // backend retorna texto simples
      setMessage(text);
    } catch (error) {
      console.error(error);
      setMessage("Erro ao conectar com o servidor.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-box">
        <h2 className="login-title">Esqueci minha senha</h2>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>E-mail cadastrado</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <button type="submit" disabled={loading}>
            {loading ? "Enviando..." : "Enviar link de redefinição"}
          </button>
        </form>

        {message && <p className="message">{message}</p>}

        <p style={{ marginTop: "10px" }}>
          <Link to="/login">Voltar ao login</Link>
        </p>
      </div>
    </div>
  );
}

import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { API_BASE_URL } from "../config";

function Login() {
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [message, setMessage] = useState("");
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, senha }),
      });

      if (response.ok) {
        const data = await response.json();
        
        // Salva token e informações do usuário
        localStorage.setItem("token", data.token);
        localStorage.setItem("usuario", JSON.stringify(data.usuario));

        setMessage("Login realizado com sucesso!");
        navigate("/home");
      } 
      else {
        const error = await response.text();
        setMessage(error);
      }
    } catch (error) {
      setMessage("Erro de conexão com o servidor.");
    }
  };

  return (
    <div style={styles.background}>
      <div style={styles.card}>
        <h2 style={styles.title}>Portal EduRH</h2>

        <form onSubmit={handleLogin} style={styles.form}>
          <input
            type="email"
            placeholder="E-mail"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            style={styles.input}
          />
          <input
            type="password"
            placeholder="Senha"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            required
            style={styles.input}
          />
          <button type="submit" style={styles.button}>
            Entrar
          </button>
        </form>

        {message && <p style={styles.message}>{message}</p>}

        <div style={styles.links}>
          <Link to="/esqueci-senha" style={styles.link}>
            Esqueceu a senha?
          </Link>
          <Link to="/cadastro" style={{ ...styles.link, marginTop: "8px" }}>
            Criar nova conta
          </Link>
        </div>
      </div>
    </div>
  );
}

const styles = {
  background: {
    minHeight: "100vh",
    background: "linear-gradient(135deg, #dbeafe 0%, #ffffff 50%, #bfdbfe 100%)",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    fontFamily: "Arial, sans-serif",
  },
  card: {
    backgroundColor: "white",
    borderRadius: "16px",
    boxShadow: "0 4px 12px rgba(0,0,0,0.1)",
    padding: "40px",
    width: "100%",
    maxWidth: "380px",
    textAlign: "center",
  },
  title: {
    fontSize: "28px",
    fontWeight: "bold",
    color: "#1d4ed8",
    marginBottom: "25px",
  },
  form: {
    display: "flex",
    flexDirection: "column",
    gap: "15px",
  },
  input: {
    padding: "12px",
    borderRadius: "8px",
    border: "1px solid #ccc",
    fontSize: "15px",
    outline: "none",
  },
  button: {
    backgroundColor: "#2563eb",
    color: "white",
    padding: "12px",
    border: "none",
    borderRadius: "8px",
    fontSize: "16px",
    cursor: "pointer",
    transition: "0.3s",
  },
  message: {
    marginTop: "12px",
    fontSize: "14px",
    color: "#555",
  },
  links: {
    marginTop: "18px",
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    fontSize: "14px",
  },
  link: {
    color: "#2563eb",
    textDecoration: "none",
    transition: "color 0.2s",
  },
};

export default Login;

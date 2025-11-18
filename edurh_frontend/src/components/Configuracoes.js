import React, { useEffect, useState } from "react";
import "../styles/Configuracoes.css";
import BotaoVoltar from "./BotaoVoltar";
import { API_BASE_URL } from "../config";

export default function Configuracoes() {
  const [usuarioId, setUsuarioId] = useState(null);
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senhaAtual, setSenhaAtual] = useState("");
  const [novaSenha, setNovaSenha] = useState("");
  const [confirmarSenha, setConfirmarSenha] = useState("");
  const [mensagem, setMensagem] = useState("");
  const [erro, setErro] = useState("");

  const token = localStorage.getItem("token");

  const headersAuth = {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  };

  // 1) Preenche com o que tiver no localStorage (caso já exista)
  useEffect(() => {
    const nomeLS = localStorage.getItem("nomeUsuario");
    const emailLS = localStorage.getItem("emailUsuario");

    if (nomeLS) setNome(nomeLS);
    if (emailLS) setEmail(emailLS);
  }, []);

  // 2) Busca o usuário logado no backend (/auth/me) e pega id, nome e email
  useEffect(() => {
    async function carregarUsuario() {
      try {
        if (!token) {
          setErro("Usuário não autenticado. Faça login novamente.");
          return;
        }

        const res = await fetch(`${API_BASE_URL}/auth/me`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (!res.ok) {
          const txt = await res.text();
          console.error("Erro ao buscar /auth/me:", res.status, txt);
          setErro("Erro ao carregar dados do usuário. Faça login novamente.");
          return;
        }

        const data = await res.json();

        // aqui assumo que o backend retorna { id, nome, email, ... }
        setUsuarioId(data.id);
        setNome(data.nome || "");
        setEmail(data.email || "");

        // atualiza localStorage também
        localStorage.setItem("nomeUsuario", data.nome || "");
        localStorage.setItem("emailUsuario", data.email || "");
      } catch (e) {
        console.error("Erro ao carregar usuário:", e);
        setErro("Erro de conexão ao carregar usuário.");
      }
    }

    carregarUsuario();
  }, [token]);

  // Salvar nome/email
  const handleSalvar = async (e) => {
    e.preventDefault();
    setMensagem("");
    setErro("");

    try {
      if (!usuarioId) {
        setErro("Não foi possível identificar o usuário. Faça login novamente.");
        return;
      }

      const res = await fetch(`${API_BASE_URL}/usuarios/${usuarioId}`, {
        method: "PUT",
        headers: headersAuth,
        body: JSON.stringify({ nome, email }),
      });

      if (!res.ok) {
        const txt = await res.text();
        throw new Error(txt || "Erro ao salvar dados.");
      }

      const atualizado = await res.json();
      setNome(atualizado.nome || nome);
      setEmail(atualizado.email || email);

      localStorage.setItem("nomeUsuario", atualizado.nome || nome);
      localStorage.setItem("emailUsuario", atualizado.email || email);

      setMensagem("Alterações salvas com sucesso!");
    } catch (e) {
      console.error(e);
      setErro(e.message);
    }
  };

  // Alterar senha
  const handleAlterarSenha = async (e) => {
    e.preventDefault();
    setMensagem("");
    setErro("");

    if (!usuarioId) {
      setErro("Não foi possível identificar o usuário. Faça login novamente.");
      return;
    }

    if (novaSenha !== confirmarSenha) {
      setErro("A nova senha e a confirmação não conferem.");
      return;
    }

    try {
      const res = await fetch(`${API_BASE_URL}/usuarios/${usuarioId}/senha`, {
        method: "PUT",
        headers: headersAuth,
        body: JSON.stringify({
          senhaAtual,
          novaSenha,
        }),
      });

      const txt = await res.text();

      if (!res.ok) {
        throw new Error(txt || "Erro ao alterar senha.");
      }

      setMensagem(txt || "Senha alterada com sucesso!");
      setSenhaAtual("");
      setNovaSenha("");
      setConfirmarSenha("");
    } catch (e) {
      console.error(e);
      setErro(e.message);
    }
  };

  const handleLogout = () => {
    if (window.confirm("Deseja realmente sair?")) {
      localStorage.clear();
      window.location.href = "/login";
    }
  };

  return (
    <div className="config-container">
      <BotaoVoltar destino="/home" />

      <h2>⚙️ Configurações da Conta</h2>

      {erro && (
        <div className="mensagem-erro" style={{ color: "red" }}>
          {erro}
        </div>
      )}
      {mensagem && (
        <div className="mensagem-sucesso" style={{ color: "green" }}>
          {mensagem}
        </div>
      )}

      <div className="config-section">
        <h3>🧍 Dados Pessoais</h3>
        <form onSubmit={handleSalvar}>
          <div className="form-group">
            <label>Nome:</label>
            <input value={nome} onChange={(e) => setNome(e.target.value)} />
          </div>
          <div className="form-group">
            <label>Email:</label>
            <input value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>
          <button type="submit" className="btn salvar">
            💾 Salvar Alterações
          </button>
        </form>
      </div>

      <div className="config-section">
        <h3>🔐 Alterar Senha</h3>
        <form onSubmit={handleAlterarSenha}>
          <div className="form-group">
            <label>Senha Atual:</label>
            <input
              type="password"
              value={senhaAtual}
              onChange={(e) => setSenhaAtual(e.target.value)}
            />
          </div>
          <div className="form-group">
            <label>Nova Senha:</label>
            <input
              type="password"
              value={novaSenha}
              onChange={(e) => setNovaSenha(e.target.value)}
            />
          </div>
          <div className="form-group">
            <label>Confirmar Nova:</label>
            <input
              type="password"
              value={confirmarSenha}
              onChange={(e) => setConfirmarSenha(e.target.value)}
            />
          </div>
          <button type="submit" className="btn senha">
            🔄 Alterar Senha
          </button>
        </form>
      </div>

      <div className="config-section">
        <button className="btn sair" onClick={handleLogout}>
          🚪 Sair
        </button>
      </div>
    </div>
  );
}

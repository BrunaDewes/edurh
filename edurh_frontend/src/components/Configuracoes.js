import React, { useState } from "react";
import "../styles/Configuracoes.css";
import BotaoVoltar from "./BotaoVoltar"; 

export default function Configuracoes() {
  const [nome, setNome] = useState("Bruna");
  const [email, setEmail] = useState("brunalunkesd@gmail.com");
  const [senhaAtual, setSenhaAtual] = useState("");
  const [novaSenha, setNovaSenha] = useState("");

  const handleSalvar = (e) => {
    e.preventDefault();
    alert("Alterações salvas com sucesso!");
  };

  const handleAlterarSenha = (e) => {
    e.preventDefault();
    alert("Senha alterada!");
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

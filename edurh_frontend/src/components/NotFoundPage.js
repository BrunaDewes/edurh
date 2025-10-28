export default function NotFoundPage() {
  return (
    <div style={styles.container}>
      <h1 style={styles.title}>404</h1>
      <p style={styles.text}>Página não encontrada 😕</p>
      <a href="/login" style={styles.link}>Voltar ao login</a>
    </div>
  );
}

const styles = {
  container: {
    minHeight: "100vh",
    display: "flex",
    flexDirection: "column",
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#f3f4f6",
    color: "#333",
    fontFamily: "Arial, sans-serif",
  },
  title: {
    fontSize: "6rem",
    fontWeight: "bold",
    color: "#1d4ed8",
    marginBottom: "10px",
  },
  text: {
    fontSize: "1.25rem",
    marginBottom: "20px",
  },
  link: {
    textDecoration: "none",
    color: "white",
    backgroundColor: "#1d4ed8",
    padding: "10px 20px",
    borderRadius: "6px",
    fontWeight: "bold",
    transition: "background-color 0.3s",
  },
};

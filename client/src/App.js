import { Outlet } from "react-router-dom";
import Header from "./components/Header";
import useAxios from "./hooks/useAxios";
import PageWrapper from "./utility/PageWrapper";
import Footer from "./components/Footer";

const App = () => {
  useAxios();

  return (
    <main className="min-h-screen flex flex-col bg-background text-foreground">
      <Header />
      <PageWrapper>
        <Outlet />
      </PageWrapper>
      <Footer />
    </main>
  );
};

export default App;
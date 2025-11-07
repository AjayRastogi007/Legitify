import { Outlet } from "react-router-dom";
import Header from "./components/Header";
import useAxios from "./hooks/useAxios";
import PageWrapper from "./components/PageWrapper";

const App = () => {
  useAxios();

  return (
    <main>
      <Header />
      <PageWrapper>
        <Outlet />
      </PageWrapper>
    </main>
  );
};

export default App;
import { Outlet } from "react-router-dom";
import Header from "./components/Header"
import useAxios from "./hooks/useAxios";

const App = () => {
  useAxios();
  return (
    <main>
      <Header></Header>
      <Outlet />
    </main>
  );
};

export default App;

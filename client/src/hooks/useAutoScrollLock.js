import { useEffect } from "react";

const useAutoScrollLock = () => {
  useEffect(() => {
    document.documentElement.style.overflowY = "hidden";
    document.body.style.overflowY = "hidden";
    return () => {
      document.documentElement.style.overflowY = "auto";
      document.body.style.overflowY = "auto";
    };
  }, []);
};

export default useAutoScrollLock;

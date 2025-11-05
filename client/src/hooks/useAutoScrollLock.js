import { useEffect } from "react";

export default function useAutoScrollLock() {
  useEffect(() => {
    const checkScroll = () => {
      const contentHeight = document.body.scrollHeight;
      const viewportHeight = window.innerHeight;

      if (contentHeight <= viewportHeight) {
        document.body.style.overflow = "hidden";
      } else {
        document.body.style.overflow = "auto";
      }
    };

    checkScroll();
    window.addEventListener("resize", checkScroll);

    return () => {
      window.removeEventListener("resize", checkScroll);
      document.body.style.overflow = "auto";
    };
  }, []);
}

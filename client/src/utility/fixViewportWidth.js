export const fixViewportWidth = () => {
  const updateVW = () => {
    const vw = window.innerWidth * 0.01;
    document.documentElement.style.setProperty("--vw", `${vw}px`);
  };

  updateVW();
  window.addEventListener("resize", updateVW);
};

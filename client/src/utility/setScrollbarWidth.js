export const setScrollbarWidth = () => {
  const calc = () => {
    const scrollDiv = document.createElement("div");
    scrollDiv.style.visibility = "hidden";
    scrollDiv.style.overflow = "scroll";
    scrollDiv.style.width = "100px";
    scrollDiv.style.position = "absolute";
    scrollDiv.style.top = "-9999px";
    document.body.appendChild(scrollDiv);

    const innerDiv = document.createElement("div");
    innerDiv.style.width = "100%";
    scrollDiv.appendChild(innerDiv);

    const scrollbarWidth = scrollDiv.offsetWidth - innerDiv.offsetWidth;
    document.documentElement.style.setProperty("--scrollbar-width", `${scrollbarWidth}px`);

    document.body.removeChild(scrollDiv);
  };

  calc();
  window.addEventListener("resize", calc);
};

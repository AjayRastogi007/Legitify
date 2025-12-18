const PageWrapper = ({ children }) => {
  return (
    <div className="page-content flex-1 flex flex-col px-4 md:px-8 pt-4 pb-10">
      {children}
    </div>
  );
};

export default PageWrapper;

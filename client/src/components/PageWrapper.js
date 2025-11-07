import React from 'react';

const PageWrapper = ({ children, className = '' }) => {
  return (
    <div className={`page-content ${className}`}>
      <div style={{ width: '100%', maxWidth: 980 }}>
        {children}
      </div>
    </div>
  );
};

export default PageWrapper;

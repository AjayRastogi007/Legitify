import React, { useState } from 'react';
import { FilePond, registerPlugin } from 'react-filepond';
import FilePondPluginFileValidateType from 'filepond-plugin-file-validate-type';
import 'filepond/dist/filepond.min.css';

registerPlugin(FilePondPluginFileValidateType);

const PdfUploader = () => {
  const [files, setFiles] = useState([]);

  return (
    <div
      className="d-flex flex-column align-items-center justify-content-center px-3 flex-grow-1"
      style={{
        flex: 1,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
      }}
    >
      <div
        className="form-card p-4 p-md-5 shadow-sm w-100"
        style={{
          maxWidth: '480px',
          borderTop: '5px solid var(--brand-accent)',
          transition: 'all var(--transition-slow)',
        }}
      >

        <FilePond
          files={files}
          onupdatefiles={setFiles}
          allowMultiple={true}
          maxFiles={3}
          maxParallelUploads={1}
          allowFileTypeValidation={true}
          allowFileEncode={true}
          allowReplace={true}
          labelFileTypeNotAllowed="Invalid file type"
          fileValidateTypeLabelExpectedTypes="Only PDF, DOCX, or TXT files are allowed"
          dropOnPage={true}
          acceptedFileTypes={[
            'application/pdf',
            'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
            'text/plain',
          ]}
          labelIdle='Drag & Drop your file(s) or <span class="filepond--label-action">Browse</span>'
        />

        <p className="text-muted small mb-0 text-center mt-3">
          Supports PDF, DOCX, and TXT
        </p>
      </div>
    </div>
  );
};

export default PdfUploader;

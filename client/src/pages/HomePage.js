import PdfUploader from "../components/PdfUploader";
import useAutoScrollLock from "../hooks/useAutoScrollLock";

const HomePage = () => {
    useAutoScrollLock();
    return (
        <PdfUploader />
    );
}

export default HomePage;
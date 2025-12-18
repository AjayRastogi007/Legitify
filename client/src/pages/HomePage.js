import PdfUploader from "../components/PdfUploader";
import FeaturesSection from "../components/FeaturesSection";

const HomePage = () => {
  return (
    <main className="flex-1 flex flex-col">
      <section className="home-hero text-center max-w-4xl mx-auto pt-6 pb-10 px-6">
        <h1 className="text-4xl md:text-5xl lg:text-6xl font-extrabold leading-tight mb-4 text-foreground">
          Analyze Legal Documents <br />
          <span className="text-primary">with Confidence</span>
        </h1>

        <p className="text-base md:text-lg text-muted-foreground max-w-2xl mx-auto">
          Upload your legal documents and get instant AI-powered analysis,
          risk assessment, and actionable insights.
        </p>
      </section>

      <section className="home-uploader flex flex-col items-center gap-8 md:gap-15">
        <PdfUploader />
        <FeaturesSection />
      </section>

    </main>
  );
};

export default HomePage;
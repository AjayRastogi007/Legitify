import { FileSearch, ShieldCheck, Zap } from "lucide-react";
import FeatureCard from "../utility/FeatureCard";

const features = [
    {
        icon: FileSearch,
        title: "Smart Analysis",
        description:
            "AI-powered document parsing identifies key clauses and potential issues.",
    },
    {
        icon: ShieldCheck,
        title: "Risk Detection",
        description:
            "Automatically flags risky terms and unusual contract provisions.",
    },
    {
        icon: Zap,
        title: "Instant Results",
        description:
            "Get comprehensive analysis in seconds, not hours.",
    },
];

const FeaturesSection = () => {
    return (
        <section>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {features.map((feature) => (
                    <FeatureCard
                        key={feature.title}
                        {...feature}
                    />
                ))}
            </div>
        </section>
    );
};

export default FeaturesSection;
const FeatureCard = ({ icon: Icon, title, description }) => {
    return (
        <div
            className=" bg-card rounded-2xl p-6 border border-border  shadow-sm hover:shadow-md transition-shadow duration-200 flex flex-col gap-4"
        >
            <div className="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center">
                <Icon className="w-6 h-6 text-primary" />
            </div>

            <h3 className="text-lg font-semibold text-foreground">
                {title}
            </h3>

            <p className="text-sm text-muted-foreground leading-relaxed">
                {description}
            </p>
        </div>
    );
};

export default FeatureCard;

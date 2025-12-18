import { Github, Linkedin } from "lucide-react";

const Footer = () => {
    return (
        <footer className="border-t border-border">
            <div className="max-w-6xl mx-auto px-6 py-8 flex flex-col md:flex-row items-center justify-between gap-4">
                <p className="text-sm text-muted-foreground">
                    Built by{" "}
                    <span className="font-medium text-foreground">
                        Ajay Rastogi
                    </span>
                </p>

                <div className="flex items-center gap-7">
                    <a
                        href="https://github.com/AjayRastogi007"
                        target="_blank"
                        rel="noopener noreferrer"
                        aria-label="GitHub"
                        className="text-muted-foreground hover:text-foreground transition-colors"
                    >
                        <Github className="w-5 h-5" />
                    </a>

                    <a
                        href="https://www.linkedin.com/in/ajay-rastogi-0b0888295"
                        target="_blank"
                        rel="noopener noreferrer"
                        aria-label="LinkedIn"
                        className="text-muted-foreground hover:text-foreground transition-colors"
                    >
                        <Linkedin className="w-5 h-5" />
                    </a>
                </div>
            </div>
        </footer>
    );
};

export default Footer;

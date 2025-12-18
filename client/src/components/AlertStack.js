import { X } from "lucide-react";

const AlertStack = ({ alerts, onRemove }) => {
  if (!alerts || alerts.length === 0) return null;

  return (
    <div className="fixed top-18 right-4 z-50 flex flex-col gap-2 max-w-sm">
      {alerts.map((alert) => (
        <div
          key={alert.id}
          className={`${alert.bg} ${alert.color} ${alert.border} border rounded-lg p-4 shadow-lg animate-fade-up flex items-start gap-3`}
        >
          <div className="flex-1">
            <h4 className="font-semibold text-sm">{alert.title}</h4>
            <p className="text-sm opacity-90 mt-1">{alert.message}</p>
          </div>
          {onRemove && (
            <button
              onClick={() => onRemove(alert.id)}
              className="opacity-70 hover:opacity-100 transition-opacity"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>
      ))}
    </div>
  );
};

export default AlertStack;
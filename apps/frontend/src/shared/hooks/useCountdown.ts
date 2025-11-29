import { useState, useEffect, useRef } from 'react';

/**
 * Hook personalizado para crear un contador regresivo
 * 
 * @param initialSeconds - Segundos iniciales para el contador
 * @param onComplete - Callback cuando el contador llega a 0
 * @returns Objeto con { seconds, isActive, start, stop, reset }
 * 
 * @example
 * ```tsx
 * const { seconds, start, reset } = useCountdown(60, () => {
 *   console.log('Countdown completed!');
 * });
 * 
 * // Iniciar contador
 * start();
 * 
 * // Mostrar tiempo restante
 * <div>{Math.floor(seconds / 60)}:{(seconds % 60).toString().padStart(2, '0')}</div>
 * ```
 */
export function useCountdown(
  initialSeconds: number,
  onComplete?: () => void
) {
  const [seconds, setSeconds] = useState(initialSeconds);
  const [isActive, setIsActive] = useState(false);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    if (isActive && seconds > 0) {
      intervalRef.current = setInterval(() => {
        setSeconds((prev) => {
          if (prev <= 1) {
            setIsActive(false);
            onComplete?.();
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } else {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    }

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, [isActive, seconds, onComplete]);

  const start = () => {
    setIsActive(true);
  };

  const stop = () => {
    setIsActive(false);
  };

  const reset = (newSeconds?: number) => {
    setIsActive(false);
    setSeconds(newSeconds ?? initialSeconds);
  };

  return {
    seconds,
    isActive,
    start,
    stop,
    reset,
  };
}


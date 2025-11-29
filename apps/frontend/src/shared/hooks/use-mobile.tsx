import * as React from "react";

/** Breakpoint para considerar dispositivo móvil (768px) */
const MOBILE_BREAKPOINT = 768;

/**
 * Hook para detectar si el dispositivo es móvil.
 *
 * Utiliza media queries para detectar el ancho de la ventana y determinar
 * si el usuario está en un dispositivo móvil (ancho < 768px).
 * Se actualiza automáticamente al redimensionar la ventana.
 *
 * @hook
 *
 * @returns {boolean} `true` si el ancho de ventana es menor a 768px
 *
 * @example
 * ```tsx
 * function MyComponent() {
 *   const isMobile = useIsMobile();
 *
 *   return (
 *     <div>
 *       {isMobile ? <MobileLayout /> : <DesktopLayout />}
 *     </div>
 *   );
 * }
 * ```
 */
export function useIsMobile() {
  const [isMobile, setIsMobile] = React.useState<boolean | undefined>(undefined);

  React.useEffect(() => {
    const mql = window.matchMedia(`(max-width: ${MOBILE_BREAKPOINT - 1}px)`);
    const onChange = () => {
      setIsMobile(window.innerWidth < MOBILE_BREAKPOINT);
    };
    mql.addEventListener("change", onChange);
    setIsMobile(window.innerWidth < MOBILE_BREAKPOINT);
    return () => mql.removeEventListener("change", onChange);
  }, []);

  return !!isMobile;
}

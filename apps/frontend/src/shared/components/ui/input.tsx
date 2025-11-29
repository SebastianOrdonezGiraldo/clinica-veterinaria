import * as React from "react";

import { cn } from "@shared/utils/utils";

/**
 * Componente Input para entrada de texto.
 *
 * Campo de entrada estilizado con soporte para diferentes tipos (text, email, password, etc.).
 * Incluye estados visuales para focus, disabled y validación.
 *
 * @component
 *
 * @param {React.ComponentProps<"input">} props - Propiedades estándar de input HTML
 * @param {string} [props.type] - Tipo de input (text, email, password, number, etc.)
 * @param {string} [props.className] - Clases CSS adicionales
 * @param {string} [props.placeholder] - Texto placeholder
 * @param {boolean} [props.disabled] - Si está deshabilitado
 *
 * @returns {JSX.Element} El componente Input renderizado
 *
 * @example
 * ```tsx
 * // Input básico
 * <Input placeholder="Escribe aquí..." />
 *
 * // Input de email
 * <Input type="email" placeholder="email@ejemplo.com" />
 *
 * // Input deshabilitado
 * <Input disabled value="No editable" />
 * ```
 */
const Input = React.forwardRef<HTMLInputElement, React.ComponentProps<"input">>(
  ({ className, type, ...props }, ref) => {
    return (
      <input
        type={type}
        className={cn(
          "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-base ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium file:text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 md:text-sm",
          className,
        )}
        ref={ref}
        {...props}
      />
    );
  },
);
Input.displayName = "Input";

export { Input };

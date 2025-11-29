import * as React from "react";
import { Slot } from "@radix-ui/react-slot";
import { cva, type VariantProps } from "class-variance-authority";

import { cn } from "@shared/utils/utils";

/**
 * Variantes de estilo para el componente Button.
 * Define las clases CSS para diferentes variantes y tamaños del botón.
 *
 * @see {@link Button}
 */
const buttonVariants = cva(
  "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:size-4 [&_svg]:shrink-0",
  {
    variants: {
      variant: {
        default: "bg-primary text-primary-foreground hover:bg-primary/90",
        destructive: "bg-destructive text-destructive-foreground hover:bg-destructive/90",
        outline: "border border-input bg-background hover:bg-accent hover:text-accent-foreground",
        secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80",
        ghost: "hover:bg-accent hover:text-accent-foreground",
        link: "text-primary underline-offset-4 hover:underline",
      },
      size: {
        default: "h-10 px-4 py-2",
        sm: "h-9 rounded-md px-3",
        lg: "h-11 rounded-md px-8",
        icon: "h-10 w-10",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  },
);

/**
 * Propiedades del componente Button.
 *
 * @interface ButtonProps
 * @extends {React.ButtonHTMLAttributes<HTMLButtonElement>}
 * @extends {VariantProps<typeof buttonVariants>}
 */
export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  /** Si es true, el componente se renderiza como su elemento hijo usando Radix Slot */
  asChild?: boolean;
}

/**
 * Componente Button reutilizable con múltiples variantes y tamaños.
 *
 * Soporta diferentes estilos visuales (default, destructive, outline, secondary, ghost, link)
 * y tamaños (default, sm, lg, icon). Puede ser usado como un botón normal o como wrapper
 * de otro elemento usando la prop `asChild`.
 *
 * @component
 *
 * @param {ButtonProps} props - Propiedades del componente
 * @param {string} [props.variant='default'] - Variante visual del botón
 * @param {string} [props.size='default'] - Tamaño del botón
 * @param {boolean} [props.asChild=false] - Si es true, renderiza el hijo directo en lugar del botón
 * @param {string} [props.className] - Clases CSS adicionales
 *
 * @returns {JSX.Element} El componente Button renderizado
 *
 * @example
 * ```tsx
 * // Botón primario
 * <Button>Click me</Button>
 *
 * // Botón destructivo pequeño
 * <Button variant="destructive" size="sm">Delete</Button>
 *
 * // Botón con icono
 * <Button size="icon"><Plus className="h-4 w-4" /></Button>
 *
 * // Como enlace usando asChild
 * <Button asChild>
 *   <a href="/page">Go to page</a>
 * </Button>
 * ```
 *
 * @see {@link buttonVariants}
 */
const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : "button";
    return <Comp className={cn(buttonVariants({ variant, size, className }))} ref={ref} {...props} />;
  },
);
Button.displayName = "Button";

export { Button, buttonVariants };

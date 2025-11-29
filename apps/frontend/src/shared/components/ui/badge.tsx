import * as React from "react";
import { cva, type VariantProps } from "class-variance-authority";

import { cn } from "@shared/utils/utils";

/**
 * Variantes de estilo para el componente Badge.
 * Define las clases CSS para diferentes estilos visuales.
 *
 * @see {@link Badge}
 */
const badgeVariants = cva(
  "inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2",
  {
    variants: {
      variant: {
        default: "border-transparent bg-primary text-primary-foreground hover:bg-primary/80",
        secondary: "border-transparent bg-secondary text-secondary-foreground hover:bg-secondary/80",
        destructive: "border-transparent bg-destructive text-destructive-foreground hover:bg-destructive/80",
        outline: "text-foreground",
      },
    },
    defaultVariants: {
      variant: "default",
    },
  },
);

/**
 * Propiedades del componente Badge.
 *
 * @interface BadgeProps
 * @extends {React.HTMLAttributes<HTMLDivElement>}
 * @extends {VariantProps<typeof badgeVariants>}
 */
export interface BadgeProps extends React.HTMLAttributes<HTMLDivElement>, VariantProps<typeof badgeVariants> {}

/**
 * Componente Badge para mostrar etiquetas o indicadores de estado.
 *
 * Elemento compacto que muestra información corta como estados, categorías o contadores.
 * Soporta diferentes variantes visuales: default, secondary, destructive y outline.
 *
 * @component
 *
 * @param {BadgeProps} props - Propiedades del componente
 * @param {string} [props.variant='default'] - Variante visual del badge
 * @param {string} [props.className] - Clases CSS adicionales
 *
 * @returns {JSX.Element} El componente Badge renderizado
 *
 * @example
 * ```tsx
 * // Badge por defecto
 * <Badge>Nuevo</Badge>
 *
 * // Badge destructivo para alertas
 * <Badge variant="destructive">Error</Badge>
 *
 * // Badge outline
 * <Badge variant="outline">En proceso</Badge>
 * ```
 *
 * @see {@link badgeVariants}
 */
function Badge({ className, variant, ...props }: BadgeProps) {
  return <div className={cn(badgeVariants({ variant }), className)} {...props} />;
}

export { Badge, badgeVariants };

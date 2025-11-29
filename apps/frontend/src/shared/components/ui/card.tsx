import * as React from "react";

import { cn } from "@shared/utils/utils";

/**
 * Componente Card contenedor principal.
 *
 * Proporciona un contenedor con bordes redondeados, sombra sutil y fondo temático.
 * Se utiliza como wrapper para agrupar contenido relacionado visualmente.
 *
 * @component
 *
 * @param {React.HTMLAttributes<HTMLDivElement>} props - Propiedades del elemento div
 * @param {string} [props.className] - Clases CSS adicionales
 *
 * @returns {JSX.Element} El componente Card renderizado
 *
 * @example
 * ```tsx
 * <Card>
 *   <CardHeader>
 *     <CardTitle>Título</CardTitle>
 *     <CardDescription>Descripción</CardDescription>
 *   </CardHeader>
 *   <CardContent>Contenido principal</CardContent>
 *   <CardFooter>Pie de tarjeta</CardFooter>
 * </Card>
 * ```
 *
 * @see {@link CardHeader}
 * @see {@link CardContent}
 * @see {@link CardFooter}
 */
const Card = React.forwardRef<HTMLDivElement, React.HTMLAttributes<HTMLDivElement>>(({ className, ...props }, ref) => (
  <div ref={ref} className={cn("rounded-lg border bg-card text-card-foreground shadow-sm", className)} {...props} />
));
Card.displayName = "Card";

/**
 * Componente CardHeader para el encabezado de la tarjeta.
 *
 * Proporciona espaciado y diseño para el título y descripción de la tarjeta.
 *
 * @component
 * @see {@link Card}
 */
const CardHeader = React.forwardRef<HTMLDivElement, React.HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => (
    <div ref={ref} className={cn("flex flex-col space-y-1.5 p-6", className)} {...props} />
  ),
);
CardHeader.displayName = "CardHeader";

/**
 * Componente CardTitle para el título de la tarjeta.
 *
 * Renderiza un heading h3 con estilos predefinidos.
 *
 * @component
 * @see {@link Card}
 */
const CardTitle = React.forwardRef<HTMLParagraphElement, React.HTMLAttributes<HTMLHeadingElement>>(
  ({ className, ...props }, ref) => (
    <h3 ref={ref} className={cn("text-2xl font-semibold leading-none tracking-tight", className)} {...props} />
  ),
);
CardTitle.displayName = "CardTitle";

/**
 * Componente CardDescription para la descripción de la tarjeta.
 *
 * Renderiza texto secundario con estilo muted.
 *
 * @component
 * @see {@link Card}
 */
const CardDescription = React.forwardRef<HTMLParagraphElement, React.HTMLAttributes<HTMLParagraphElement>>(
  ({ className, ...props }, ref) => (
    <p ref={ref} className={cn("text-sm text-muted-foreground", className)} {...props} />
  ),
);
CardDescription.displayName = "CardDescription";

/**
 * Componente CardContent para el contenido principal de la tarjeta.
 *
 * Proporciona padding horizontal y vertical para el contenido.
 *
 * @component
 * @see {@link Card}
 */
const CardContent = React.forwardRef<HTMLDivElement, React.HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => <div ref={ref} className={cn("p-6 pt-0", className)} {...props} />,
);
CardContent.displayName = "CardContent";

/**
 * Componente CardFooter para el pie de la tarjeta.
 *
 * Diseñado para acciones o información adicional al final de la tarjeta.
 *
 * @component
 * @see {@link Card}
 */
const CardFooter = React.forwardRef<HTMLDivElement, React.HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => (
    <div ref={ref} className={cn("flex items-center p-6 pt-0", className)} {...props} />
  ),
);
CardFooter.displayName = "CardFooter";

export { Card, CardHeader, CardFooter, CardTitle, CardDescription, CardContent };

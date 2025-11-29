import * as React from "react";

import { cn } from "@shared/utils/utils";

/**
 * Componente Table principal.
 *
 * Tabla HTML estilizada con soporte para responsive design mediante scroll horizontal.
 * Se utiliza junto con los componentes TableHeader, TableBody, TableRow, etc.
 *
 * @component
 *
 * @param {React.HTMLAttributes<HTMLTableElement>} props - Propiedades del elemento table
 * @param {string} [props.className] - Clases CSS adicionales
 *
 * @returns {JSX.Element} El componente Table renderizado
 *
 * @example
 * ```tsx
 * <Table>
 *   <TableHeader>
 *     <TableRow>
 *       <TableHead>Nombre</TableHead>
 *       <TableHead>Email</TableHead>
 *     </TableRow>
 *   </TableHeader>
 *   <TableBody>
 *     <TableRow>
 *       <TableCell>Juan</TableCell>
 *       <TableCell>juan@email.com</TableCell>
 *     </TableRow>
 *   </TableBody>
 * </Table>
 * ```
 *
 * @see {@link TableHeader}
 * @see {@link TableBody}
 * @see {@link TableRow}
 * @see {@link TableCell}
 */
const Table = React.forwardRef<HTMLTableElement, React.HTMLAttributes<HTMLTableElement>>(
  ({ className, ...props }, ref) => (
    <div className="relative w-full overflow-auto">
      <table ref={ref} className={cn("w-full caption-bottom text-sm", className)} {...props} />
    </div>
  ),
);
Table.displayName = "Table";

/**
 * Componente TableHeader para el encabezado de la tabla.
 * Contenedor para las filas de encabezado (TableRow con TableHead).
 * @component
 * @see {@link Table}
 */
const TableHeader = React.forwardRef<HTMLTableSectionElement, React.HTMLAttributes<HTMLTableSectionElement>>(
  ({ className, ...props }, ref) => <thead ref={ref} className={cn("[&_tr]:border-b", className)} {...props} />,
);
TableHeader.displayName = "TableHeader";

/**
 * Componente TableBody para el cuerpo de la tabla.
 * Contenedor para las filas de datos.
 * @component
 * @see {@link Table}
 */
const TableBody = React.forwardRef<HTMLTableSectionElement, React.HTMLAttributes<HTMLTableSectionElement>>(
  ({ className, ...props }, ref) => (
    <tbody ref={ref} className={cn("[&_tr:last-child]:border-0", className)} {...props} />
  ),
);
TableBody.displayName = "TableBody";

/**
 * Componente TableFooter para el pie de la tabla.
 * Útil para totales o información resumida.
 * @component
 * @see {@link Table}
 */
const TableFooter = React.forwardRef<HTMLTableSectionElement, React.HTMLAttributes<HTMLTableSectionElement>>(
  ({ className, ...props }, ref) => (
    <tfoot ref={ref} className={cn("border-t bg-muted/50 font-medium [&>tr]:last:border-b-0", className)} {...props} />
  ),
);
TableFooter.displayName = "TableFooter";

/**
 * Componente TableRow para filas de la tabla.
 * Incluye hover effect y estado seleccionado.
 * @component
 * @see {@link Table}
 */
const TableRow = React.forwardRef<HTMLTableRowElement, React.HTMLAttributes<HTMLTableRowElement>>(
  ({ className, ...props }, ref) => (
    <tr
      ref={ref}
      className={cn("border-b transition-colors data-[state=selected]:bg-muted hover:bg-muted/50", className)}
      {...props}
    />
  ),
);
TableRow.displayName = "TableRow";

/**
 * Componente TableHead para celdas de encabezado.
 * Estilizado con texto muted y font medium.
 * @component
 * @see {@link Table}
 */
const TableHead = React.forwardRef<HTMLTableCellElement, React.ThHTMLAttributes<HTMLTableCellElement>>(
  ({ className, ...props }, ref) => (
    <th
      ref={ref}
      className={cn(
        "h-12 px-4 text-left align-middle font-medium text-muted-foreground [&:has([role=checkbox])]:pr-0",
        className,
      )}
      {...props}
    />
  ),
);
TableHead.displayName = "TableHead";

/**
 * Componente TableCell para celdas de datos.
 * Proporciona padding y alineación consistente.
 * @component
 * @see {@link Table}
 */
const TableCell = React.forwardRef<HTMLTableCellElement, React.TdHTMLAttributes<HTMLTableCellElement>>(
  ({ className, ...props }, ref) => (
    <td ref={ref} className={cn("p-4 align-middle [&:has([role=checkbox])]:pr-0", className)} {...props} />
  ),
);
TableCell.displayName = "TableCell";

/**
 * Componente TableCaption para el título de la tabla.
 * Se muestra en la parte inferior de la tabla.
 * @component
 * @see {@link Table}
 */
const TableCaption = React.forwardRef<HTMLTableCaptionElement, React.HTMLAttributes<HTMLTableCaptionElement>>(
  ({ className, ...props }, ref) => (
    <caption ref={ref} className={cn("mt-4 text-sm text-muted-foreground", className)} {...props} />
  ),
);
TableCaption.displayName = "TableCaption";

export { Table, TableHeader, TableBody, TableFooter, TableHead, TableRow, TableCell, TableCaption };

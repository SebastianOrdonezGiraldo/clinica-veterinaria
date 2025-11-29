import type { Meta, StoryObj } from '@storybook/react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogClose,
} from './dialog';
import { Button } from './button';
import { Input } from './input';

/**
 * Dialog es un componente modal para mostrar contenido que requiere atención.
 *
 * ## Componentes
 * - `Dialog`: Contenedor principal
 * - `DialogTrigger`: Elemento que abre el dialog
 * - `DialogContent`: Contenido del modal
 * - `DialogHeader`: Encabezado
 * - `DialogTitle`: Título
 * - `DialogDescription`: Descripción
 * - `DialogFooter`: Pie con acciones
 * - `DialogClose`: Botón de cierre
 */
const meta: Meta<typeof Dialog> = {
  title: 'UI/Dialog',
  component: Dialog,
  tags: ['autodocs'],
  parameters: {
    docs: {
      description: {
        component: 'Modal accesible para formularios, confirmaciones y contenido interactivo.',
      },
    },
  },
};

export default meta;
type Story = StoryObj<typeof Dialog>;

/**
 * Dialog básico
 */
export const Default: Story = {
  render: () => (
    <Dialog>
      <DialogTrigger asChild>
        <Button variant="outline">Abrir Dialog</Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Título del Dialog</DialogTitle>
          <DialogDescription>
            Esta es la descripción del dialog. Aquí puedes explicar el propósito del contenido.
          </DialogDescription>
        </DialogHeader>
        <div className="py-4">
          <p>Contenido del dialog aquí...</p>
        </div>
        <DialogFooter>
          <DialogClose asChild>
            <Button variant="outline">Cancelar</Button>
          </DialogClose>
          <Button>Confirmar</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  ),
};

/**
 * Dialog con formulario
 */
export const WithForm: Story = {
  render: () => (
    <Dialog>
      <DialogTrigger asChild>
        <Button>Editar Perfil</Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Editar Perfil</DialogTitle>
          <DialogDescription>
            Actualiza tu información personal aquí.
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-4 py-4">
          <div className="grid gap-2">
            <label className="text-sm font-medium">Nombre</label>
            <Input placeholder="Tu nombre" defaultValue="Juan Pérez" />
          </div>
          <div className="grid gap-2">
            <label className="text-sm font-medium">Email</label>
            <Input type="email" placeholder="email@ejemplo.com" defaultValue="juan@ejemplo.com" />
          </div>
        </div>
        <DialogFooter>
          <DialogClose asChild>
            <Button variant="outline">Cancelar</Button>
          </DialogClose>
          <Button type="submit">Guardar cambios</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  ),
};

/**
 * Dialog de nuevo paciente
 */
export const NuevoPaciente: Story = {
  render: () => (
    <Dialog>
      <DialogTrigger asChild>
        <Button>+ Nuevo Paciente</Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>Registrar Paciente</DialogTitle>
          <DialogDescription>
            Ingresa los datos del nuevo paciente.
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-4 py-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="grid gap-2">
              <label className="text-sm font-medium">Nombre</label>
              <Input placeholder="Nombre del paciente" />
            </div>
            <div className="grid gap-2">
              <label className="text-sm font-medium">Especie</label>
              <Input placeholder="Canino, Felino..." />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div className="grid gap-2">
              <label className="text-sm font-medium">Raza</label>
              <Input placeholder="Raza" />
            </div>
            <div className="grid gap-2">
              <label className="text-sm font-medium">Edad (meses)</label>
              <Input type="number" placeholder="24" />
            </div>
          </div>
        </div>
        <DialogFooter>
          <DialogClose asChild>
            <Button variant="outline">Cancelar</Button>
          </DialogClose>
          <Button>Registrar</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  ),
};

/**
 * Dialog informativo
 */
export const Informativo: Story = {
  render: () => (
    <Dialog>
      <DialogTrigger asChild>
        <Button variant="outline">Ver información</Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Información Importante</DialogTitle>
        </DialogHeader>
        <div className="py-4">
          <p className="text-sm text-muted-foreground">
            Esta es información importante que el usuario necesita conocer.
            El dialog se cierra haciendo clic fuera o presionando Escape.
          </p>
        </div>
        <DialogFooter>
          <DialogClose asChild>
            <Button>Entendido</Button>
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  ),
};

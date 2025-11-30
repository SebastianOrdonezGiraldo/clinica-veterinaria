import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Loader2, Dog, User, Calendar, Command } from 'lucide-react';
import { Input } from '@shared/components/ui/input';
import { Button } from '@shared/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@shared/components/ui/dialog';
import { ScrollArea } from '@shared/components/ui/scroll-area';
import { busquedaGlobalService, ResultadoBusqueda } from '../services/busquedaGlobalService';
import { useDebounce } from '@shared/hooks/useDebounce';
import { useLogger } from '@shared/hooks/useLogger';

interface BusquedaGlobalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function BusquedaGlobal({ open, onOpenChange }: BusquedaGlobalProps) {
  const logger = useLogger('BusquedaGlobal');
  const navigate = useNavigate();
  const [query, setQuery] = useState('');
  const [resultados, setResultados] = useState<ResultadoBusqueda[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(-1);
  const inputRef = useRef<HTMLInputElement>(null);
  const resultadosRef = useRef<HTMLDivElement>(null);

  const debouncedQuery = useDebounce(query, 300);

  useEffect(() => {
    if (open && inputRef.current) {
      inputRef.current.focus();
    }
  }, [open]);

  useEffect(() => {
    if (debouncedQuery.length >= 2) {
      buscar();
    } else {
      setResultados([]);
    }
  }, [debouncedQuery]);

  const buscar = async () => {
    if (!debouncedQuery.trim()) {
      setResultados([]);
      return;
    }

    setIsLoading(true);
    try {
      const resultadosBusqueda = await busquedaGlobalService.buscar(debouncedQuery);
      const normalizados = busquedaGlobalService.normalizarResultados(resultadosBusqueda);
      setResultados(normalizados);
      setSelectedIndex(-1);
    } catch (error) {
      logger.error('Error al realizar búsqueda global', error, {
        action: 'buscar',
        query: debouncedQuery,
      });
      setResultados([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSelect = (resultado: ResultadoBusqueda) => {
    navigate(resultado.url);
    onOpenChange(false);
    setQuery('');
    setResultados([]);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (resultados.length === 0) return;

    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        setSelectedIndex((prev) => 
          prev < resultados.length - 1 ? prev + 1 : prev
        );
        break;
      case 'ArrowUp':
        e.preventDefault();
        setSelectedIndex((prev) => (prev > 0 ? prev - 1 : -1));
        break;
      case 'Enter':
        e.preventDefault();
        if (selectedIndex >= 0 && selectedIndex < resultados.length) {
          handleSelect(resultados[selectedIndex]);
        } else if (resultados.length > 0) {
          handleSelect(resultados[0]);
        }
        break;
      case 'Escape':
        onOpenChange(false);
        break;
    }
  };

  useEffect(() => {
    if (selectedIndex >= 0 && resultadosRef.current) {
      const selectedElement = resultadosRef.current.children[selectedIndex] as HTMLElement;
      if (selectedElement) {
        selectedElement.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
      }
    }
  }, [selectedIndex]);

  const getIcon = (tipo: string) => {
    switch (tipo) {
      case 'paciente':
        return <Dog className="h-4 w-4" />;
      case 'propietario':
        return <User className="h-4 w-4" />;
      case 'cita':
        return <Calendar className="h-4 w-4" />;
      default:
        return null;
    }
  };

  const getTipoLabel = (tipo: string) => {
    switch (tipo) {
      case 'paciente':
        return 'Paciente';
      case 'propietario':
        return 'Propietario';
      case 'cita':
        return 'Cita';
      default:
        return tipo;
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[600px] p-0">
        <DialogHeader className="px-6 pt-6 pb-4">
          <DialogTitle>Búsqueda Global</DialogTitle>
          <DialogDescription>
            Busca pacientes, propietarios y citas en toda la aplicación
          </DialogDescription>
        </DialogHeader>

        <div className="px-6 pb-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              ref={inputRef}
              type="text"
              placeholder="Buscar pacientes, propietarios, citas..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={handleKeyDown}
              className="pl-10 pr-10"
            />
            {isLoading && (
              <Loader2 className="absolute right-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground animate-spin" />
            )}
          </div>

          {query.length > 0 && query.length < 2 && (
            <p className="text-xs text-muted-foreground mt-2">
              Escribe al menos 2 caracteres para buscar
            </p>
          )}

          {query.length >= 2 && !isLoading && resultados.length === 0 && (
            <p className="text-sm text-muted-foreground mt-4 text-center py-8">
              No se encontraron resultados para "{query}"
            </p>
          )}
        </div>

        {resultados.length > 0 && (
          <ScrollArea className="max-h-[400px]">
            <div ref={resultadosRef} className="px-2 pb-4">
              {resultados.map((resultado, index) => (
                <button
                  key={`${resultado.tipo}-${resultado.id}`}
                  onClick={() => handleSelect(resultado)}
                  className={`w-full text-left px-4 py-3 rounded-lg transition-colors ${
                    selectedIndex === index
                      ? 'bg-accent text-accent-foreground'
                      : 'hover:bg-accent/50'
                  }`}
                >
                  <div className="flex items-start gap-3">
                    <div className="mt-0.5">{getIcon(resultado.tipo)}</div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2">
                        <p className="font-medium truncate">{resultado.titulo}</p>
                        <span className="text-xs text-muted-foreground bg-muted px-2 py-0.5 rounded">
                          {getTipoLabel(resultado.tipo)}
                        </span>
                      </div>
                      {resultado.subtitulo && (
                        <p className="text-sm text-muted-foreground truncate mt-1">
                          {resultado.subtitulo}
                        </p>
                      )}
                    </div>
                  </div>
                </button>
              ))}
            </div>
          </ScrollArea>
        )}

        <div className="px-6 pb-4 border-t pt-3">
          <div className="flex items-center gap-4 text-xs text-muted-foreground">
            <div className="flex items-center gap-1">
              <Command className="h-3 w-3" />
              <span>Navegar</span>
            </div>
            <div className="flex items-center gap-1">
              <kbd className="px-1.5 py-0.5 bg-muted rounded text-xs">↵</kbd>
              <span>Seleccionar</span>
            </div>
            <div className="flex items-center gap-1">
              <kbd className="px-1.5 py-0.5 bg-muted rounded text-xs">Esc</kbd>
              <span>Cerrar</span>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}


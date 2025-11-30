import { useState, useEffect } from 'react';
import { FileText, Search, X } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Input } from '@shared/components/ui/input';
import { Badge } from '@shared/components/ui/badge';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@shared/components/ui/dialog';
import { ScrollArea } from '@shared/components/ui/scroll-area';
import { templateConsultaService, TemplateConsulta } from '../services/templateConsultaService';
import { cn } from '@shared/utils/utils';

interface TemplateSelectorProps {
  onSelect: (template: TemplateConsulta) => void;
  tipo: 'consulta' | 'prescripcion';
  className?: string;
}

export default function TemplateSelector({ onSelect, tipo, className }: TemplateSelectorProps) {
  const [templates, setTemplates] = useState<TemplateConsulta[]>([]);
  const [filteredTemplates, setFilteredTemplates] = useState<TemplateConsulta[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (isOpen && tipo === 'consulta') {
      loadTemplates();
    }
  }, [isOpen, tipo]);

  useEffect(() => {
    if (searchQuery.trim()) {
      const filtered = templates.filter(t =>
        t.nombre.toLowerCase().includes(searchQuery.toLowerCase()) ||
        t.categoria?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        t.descripcion?.toLowerCase().includes(searchQuery.toLowerCase())
      );
      setFilteredTemplates(filtered);
    } else {
      setFilteredTemplates(templates);
    }
  }, [searchQuery, templates]);

  const loadTemplates = async () => {
    try {
      setIsLoading(true);
      const data = await templateConsultaService.getAll();
      setTemplates(data);
      setFilteredTemplates(data);
    } catch (error) {
      console.error('Error al cargar templates:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSelect = (template: TemplateConsulta) => {
    onSelect(template);
    setIsOpen(false);
    setSearchQuery('');
  };

  const categorias = Array.from(new Set(templates.map(t => t.categoria).filter(Boolean)));

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>
        <Button variant="outline" className={cn("gap-2", className)}>
          <FileText className="h-4 w-4" />
          Usar Template
        </Button>
      </DialogTrigger>
      <DialogContent className="max-w-3xl max-h-[80vh]">
        <DialogHeader>
          <DialogTitle>Seleccionar Template de Consulta</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          {/* Búsqueda */}
          <div className="relative">
            <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Buscar por nombre, categoría o descripción..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-8"
            />
          </div>

          {/* Filtros por categoría */}
          {categorias.length > 0 && !searchQuery && (
            <div className="flex flex-wrap gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setFilteredTemplates(templates)}
              >
                Todos
              </Button>
              {categorias.map(cat => (
                <Button
                  key={cat}
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    const filtered = templates.filter(t => t.categoria === cat);
                    setFilteredTemplates(filtered);
                  }}
                >
                  {cat}
                </Button>
              ))}
            </div>
          )}

          {/* Lista de templates */}
          <ScrollArea className="h-[400px]">
            {isLoading ? (
              <div className="text-center py-8 text-muted-foreground">
                Cargando templates...
              </div>
            ) : filteredTemplates.length > 0 ? (
              <div className="space-y-2">
                {filteredTemplates.map(template => (
                  <Card
                    key={template.id}
                    className="cursor-pointer hover:bg-accent transition-colors"
                    onClick={() => handleSelect(template)}
                  >
                    <CardHeader className="pb-2">
                      <div className="flex items-center justify-between">
                        <CardTitle className="text-base">{template.nombre}</CardTitle>
                        {template.categoria && (
                          <Badge variant="secondary">{template.categoria}</Badge>
                        )}
                      </div>
                      {template.descripcion && (
                        <p className="text-sm text-muted-foreground mt-1">
                          {template.descripcion}
                        </p>
                      )}
                    </CardHeader>
                    <CardContent>
                      <div className="text-xs text-muted-foreground">
                        Usado {template.vecesUsado} {template.vecesUsado === 1 ? 'vez' : 'veces'}
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            ) : (
              <div className="text-center py-8 text-muted-foreground">
                No se encontraron templates
              </div>
            )}
          </ScrollArea>
        </div>
      </DialogContent>
    </Dialog>
  );
}


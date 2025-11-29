import { useNavigate } from 'react-router-dom';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import {
  Dog,
  Stethoscope,
  Syringe,
  Microscope,
  Activity,
  Scissors,
  Heart,
  MapPin,
  Phone,
  GraduationCap,
  Scan,
  Monitor,
  Sparkles,
  Shield,
  Users,
  MessageSquare,
  Award,
  CheckCircle2,
  PawPrint,
  ArrowRight,
  Star,
  Clock,
  Calendar,
} from 'lucide-react';

export default function LandingPage() {
  const navigate = useNavigate();

  const servicios = [
    {
      icon: Stethoscope,
      title: 'Consultas generales',
      description:
        'Consultas personalizadas para evaluar la salud de tu mascota. Examen físico completo, revisión de historia clínica y plan de manejo claro.',
      bullets: [
        'Evaluación clínica integral.',
        'Revisión de antecedentes y estilo de vida.',
        'Diagnóstico oportuno y recomendaciones de tratamiento.',
      ],
    },
    {
      icon: Activity,
      title: 'Exámenes físicos',
      description:
        'Chequeos periódicos para detectar problemas de salud de forma temprana y mantener a tu compañero en óptimas condiciones.',
      bullets: [
        'Revisión de ojos, oídos y nariz.',
        'Palpación abdominal y musculoesquelética.',
        'Evaluación del estado general de salud.',
      ],
    },
    {
      icon: Syringe,
      title: 'Vacunación',
      description:
        'Esquemas de vacunación diseñados según edad, especie, entorno y estilo de vida para prevenir enfermedades infecciosas.',
      bullets: [
        'Programas de vacunación personalizados.',
        'Asesoría sobre refuerzos y calendarios.',
        'Prevención de enfermedades infecciosas clave.',
      ],
    },
    {
      icon: Microscope,
      title: 'Análisis de laboratorio',
      description:
        'Apoyo diagnóstico mediante pruebas completas que permiten detectar enfermedades agudas y crónicas.',
      bullets: [
        'Pruebas de sangre y orina.',
        'Análisis de heces y otros fluidos.',
        'Diagnóstico de enfermedades infecciosas y crónicas.',
      ],
    },
    {
      icon: Scan,
      title: 'Radiología y ecografía',
      description:
        'Imágenes de alta calidad para evaluar estructuras óseas, órganos internos y apoyar el diagnóstico pre y postoperatorio.',
      bullets: [
        'Radiografías digitales.',
        'Ecografía para órganos internos.',
        'Valoración pre y postquirúrgica.',
      ],
    },
    {
      icon: Scissors,
      title: 'Cirugía general',
      description:
        'Procedimientos menores y mayores realizados bajo protocolos seguros y con equipos de última generación.',
      bullets: [
        'Esterilizaciones y cirugía de tejidos blandos.',
        'Tratamiento quirúrgico de enfermedades internas.',
        'Manejo avanzado de heridas y cirugía oncológica.',
      ],
    },
    {
      icon: Heart,
      title: 'Cuidado intensivo',
      description:
        'Atención especializada para pacientes críticos que requieren monitoreo constante y soporte avanzado.',
      bullets: [
        'Monitoreo continuo de signos vitales.',
        'Administración de medicamentos y fluidos.',
        'Apoyo respiratorio y cardíaco.',
      ],
    },
  ];

  const equipos = [
    {
      title: 'Monitor de signos vitales',
      description: 'Para monitorear a los animales durante la intervención quirúrgica.',
    },
    {
      title: 'Mesa quirúrgica graduable',
      description: 'Capacidad para la atención de perros y gatos de cualquier tamaño.',
    },
    {
      title: 'Rayos X digitales',
      description: 'Radiografías digitales de alta definición para diagnósticos precisos.',
    },
    {
      title: 'Succionador',
      description: 'Equipo para el manejo de derrames y hemorragias durante procedimientos.',
    },
    {
      title: 'Máquina de anestesia inhalada',
      description: 'Anestesia segura y controlada durante cirugías.',
    },
    {
      title: 'Ventilador veterinario',
      description: 'Ventilación mecánica especializada para perros y gatos.',
    },
    {
      title: 'Ecógrafo Doppler',
      description:
        'Permite medir la circulación sanguínea y realizar ecocardiogramas, estudios torácicos, articulares, musculares y vasculares.',
    },
    {
      title: 'Electrobisturí',
      description:
        'Corte y cauterización en un solo equipo: esterilizaciones, resección de órganos y cirugía de superficies.',
    },
  ];

  const valores = [
    {
      icon: Heart,
      title: 'Compasión',
      description:
        'Entendemos el valor emocional de los animales de compañía y brindamos una atención respetuosa y empática.',
    },
    {
      icon: Award,
      title: 'Excelencia',
      description:
        'Mantenemos altos estándares de calidad en nuestros servicios, procedimientos y atención médica.',
    },
    {
      icon: Sparkles,
      title: 'Innovación',
      description:
        'Buscamos constantemente nuevas tecnologías y técnicas vanguardistas para mejorar el diagnóstico y tratamiento.',
    },
    {
      icon: Shield,
      title: 'Integridad',
      description:
        'Actuamos con honestidad, transparencia y ética en todas nuestras decisiones y relaciones.',
    },
    {
      icon: CheckCircle2,
      title: 'Responsabilidad',
      description:
        'Promovemos prácticas sostenibles y un impacto positivo en la comunidad y el entorno.',
    },
    {
      icon: MessageSquare,
      title: 'Comunicación',
      description:
        'Fomentamos una comunicación clara y cercana con nuestros clientes para tomar decisiones informadas.',
    },
    {
      icon: Users,
      title: 'Respeto',
      description:
        'Valoramos la diversidad y las necesidades particulares de cada animal y su familia.',
    },
    {
      icon: GraduationCap,
      title: 'Colaboración',
      description:
        'Trabajamos en equipo para asegurar la mejor atención médica y apoyar la formación de futuros profesionales.',
    },
  ];

  const objetivosEstrategicos = [
    'Expandir nuestros servicios para incluir especialidades médicas y quirúrgicas.',
    'Implementar tecnologías innovadoras para mejorar la eficiencia y la calidad de la atención.',
    'Establecer alianzas con organizaciones de bienestar animal para promover la educación y el cuidado responsable.',
    'Desarrollar programas de educación y prevención para dueños de mascotas.',
    'Fortalecer nuestra presencia en línea y redes sociales para mejorar la comunicación con nuestros clientes.',
  ];

  return (
    <div className="min-h-screen bg-gradient-to-b from-white via-primary/5 to-white scroll-smooth">
      {/* HEADER */}
      <header className="sticky top-0 z-50 w-full border-b bg-white/80 backdrop-blur-xl shadow-sm transition-all duration-300">
        <div className="container mx-auto px-4 py-4 flex justify-between items-center">
          <button 
            type="button"
            className="flex items-center gap-3 group cursor-pointer bg-transparent border-none p-0" 
            onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
            aria-label="Volver al inicio"
          >
            <div className="h-12 w-12 rounded-full bg-gradient-to-br from-primary to-primary/60 flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform duration-300">
              <Dog className="h-6 w-6 text-white" />
            </div>
            <div>
              <h1 className="text-xl font-bold tracking-tight bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent">
                Clínica Veterinaria Universitaria Humboldt
              </h1>
              <p className="text-xs text-muted-foreground font-medium">VNZ</p>
            </div>
          </button>

          <Button 
            onClick={() => navigate('/login')} 
            size="lg" 
            className="font-semibold shadow-md hover:shadow-lg transition-all duration-300 hover:scale-105"
          >
            Iniciar Sesión
          </Button>
        </div>
      </header>

      {/* HERO */}
      <section className="relative container mx-auto px-4 py-20 lg:py-32 grid lg:grid-cols-2 gap-12 items-center overflow-hidden">
        {/* Elementos decorativos de fondo */}
        <div className="absolute inset-0 -z-10 overflow-hidden">
          <div className="absolute top-20 left-10 w-72 h-72 bg-primary/10 rounded-full blur-3xl animate-pulse"></div>
          <div className="absolute bottom-20 right-10 w-96 h-96 bg-secondary/10 rounded-full blur-3xl animate-pulse delay-1000"></div>
        </div>

        <div className="space-y-8 relative z-10">
          <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary/10 border border-primary/20 mb-4">
            <Star className="h-4 w-4 text-primary fill-primary" />
            <span className="text-sm font-semibold text-primary">Clínica Universitaria Certificada</span>
          </div>
          
          <h2 className="text-5xl lg:text-6xl font-extrabold leading-tight tracking-tight">
            Cuidado veterinario{' '}
            <span className="relative inline-block">
              <span className="text-primary relative z-10">de alta calidad</span>
              <span className="absolute bottom-2 left-0 w-full h-3 bg-primary/20 -z-0"></span>
            </span>
            <br />
            para quienes más amas
          </h2>
          
          <p className="text-xl text-muted-foreground max-w-lg leading-relaxed">
            Somos una clínica veterinaria universitaria que combina experiencia clínica,
            tecnología moderna y vocación de servicio para garantizar el bienestar de tus
            animales de compañía.
          </p>

          <div className="flex flex-wrap items-center gap-4 text-sm text-muted-foreground">
            <div className="flex items-center gap-2">
              <Clock className="h-4 w-4 text-primary" />
              <span>Horarios flexibles</span>
            </div>
            <div className="flex items-center gap-2">
              <Calendar className="h-4 w-4 text-primary" />
              <span>Citas en línea</span>
            </div>
            <div className="flex items-center gap-2">
              <Heart className="h-4 w-4 text-primary" />
              <span>Atención 24/7 emergencias</span>
            </div>
          </div>

          <div className="flex flex-col sm:flex-row gap-4 pt-4">
            <Button
              size="lg"
              className="text-lg px-8 py-6 shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-105 group"
              onClick={() => navigate('/agendar-cita')}
            >
              Solicitar Cita
              <ArrowRight className="ml-2 h-5 w-5 group-hover:translate-x-1 transition-transform" />
            </Button>
            <Button
              size="lg"
              variant="outline"
              className="text-lg px-8 py-6 border-2 hover:bg-primary/5 transition-all duration-300"
              onClick={() =>
                document.getElementById('servicios')?.scrollIntoView({ behavior: 'smooth' })
              }
            >
              Ver Servicios
            </Button>
          </div>
        </div>

        {/* HERO IMAGE */}
        <div className="relative z-10">
          <div className="relative aspect-square rounded-3xl overflow-hidden shadow-2xl group">
            {/* Gradiente animado de fondo */}
            <div className="absolute inset-0 bg-gradient-to-br from-primary via-primary/80 to-secondary/60 animate-gradient-xy"></div>
            
            {/* Patrón decorativo */}
            <div className="absolute inset-0 opacity-20">
              <div className="absolute top-10 left-10 w-32 h-32 border-4 border-white/30 rounded-full"></div>
              <div className="absolute bottom-10 right-10 w-24 h-24 border-4 border-white/30 rounded-full"></div>
            </div>
            
            {/* Contenido central */}
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="relative">
                <div className="absolute inset-0 bg-white/20 rounded-full blur-2xl group-hover:blur-3xl transition-all duration-500"></div>
                <Dog className="h-48 w-48 text-white relative z-10 group-hover:scale-110 transition-transform duration-500" />
              </div>
            </div>
            
            {/* Badges flotantes */}
            <div className="absolute top-6 right-6 bg-white/90 backdrop-blur-sm px-3 py-1.5 rounded-full shadow-lg">
              <div className="flex items-center gap-2">
                <Award className="h-4 w-4 text-primary" />
                <span className="text-xs font-semibold text-foreground">Certificada</span>
              </div>
            </div>
            
            <div className="absolute bottom-6 left-6 bg-white/90 backdrop-blur-sm px-3 py-1.5 rounded-full shadow-lg">
              <div className="flex items-center gap-2">
                <Users className="h-4 w-4 text-primary" />
                <span className="text-xs font-semibold text-foreground">+1000 pacientes</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* BENEFICIOS */}
      <section className="relative bg-gradient-to-b from-primary/5 via-white to-primary/5 py-16 border-y border-primary/10">
        <div className="container mx-auto px-4 grid md:grid-cols-3 gap-8">
          <Card className="border-2 border-primary/20 hover:border-primary/40 transition-all duration-300 hover:shadow-xl hover:-translate-y-2 bg-white/80 backdrop-blur-sm">
            <CardContent className="p-6 text-center">
              <div className="h-16 w-16 rounded-2xl bg-gradient-to-br from-primary to-primary/70 flex items-center justify-center mx-auto mb-4 shadow-lg">
                <PawPrint className="h-8 w-8 text-white" />
              </div>
              <h4 className="font-bold text-xl mb-2">Atención integral</h4>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Medicina preventiva, diagnóstico y tratamiento en un solo lugar.
              </p>
            </CardContent>
          </Card>
          
          <Card className="border-2 border-primary/20 hover:border-primary/40 transition-all duration-300 hover:shadow-xl hover:-translate-y-2 bg-white/80 backdrop-blur-sm">
            <CardContent className="p-6 text-center">
              <div className="h-16 w-16 rounded-2xl bg-gradient-to-br from-secondary to-secondary/70 flex items-center justify-center mx-auto mb-4 shadow-lg">
                <Monitor className="h-8 w-8 text-white" />
              </div>
              <h4 className="font-bold text-xl mb-2">Equipos de última tecnología</h4>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Imagenología, monitoreo y laboratorio clínico avanzados.
              </p>
            </CardContent>
          </Card>
          
          <Card className="border-2 border-primary/20 hover:border-primary/40 transition-all duration-300 hover:shadow-xl hover:-translate-y-2 bg-white/80 backdrop-blur-sm">
            <CardContent className="p-6 text-center">
              <div className="h-16 w-16 rounded-2xl bg-gradient-to-br from-primary to-secondary flex items-center justify-center mx-auto mb-4 shadow-lg">
                <Users className="h-8 w-8 text-white" />
              </div>
              <h4 className="font-bold text-xl mb-2">Clínica universitaria</h4>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Formación de futuros veterinarios bajo supervisión de expertos.
              </p>
            </CardContent>
          </Card>
        </div>
      </section>

      {/* QUIÉNES SOMOS */}
      <section className="py-24 bg-white relative overflow-hidden">
        <div className="absolute inset-0 bg-grid-pattern opacity-5"></div>
        <div className="container mx-auto px-4 max-w-4xl relative z-10">
          <div className="text-center mb-8">
            <Badge variant="outline" className="mb-4 px-4 py-1.5 text-sm font-semibold border-primary/30">
              Sobre Nosotros
            </Badge>
            <h2 className="text-4xl lg:text-5xl font-bold mb-6 bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent">
              ¿Quiénes somos?
            </h2>
          </div>
          <Card className="border-2 border-primary/10 shadow-xl bg-gradient-to-br from-white to-primary/5">
            <CardContent className="p-8 lg:p-12">
              <p className="text-lg lg:text-xl text-center text-muted-foreground leading-relaxed">
                Somos una clínica veterinaria universitaria comprometida con brindar atención
                médica integral y personalizada a los animales, priorizando su bienestar y calidad
                de vida. Nuestro equipo de expertos veterinarios y personal capacitado se esfuerza
                por proporcionar servicios de alta calidad, innovación y compasión.
              </p>
            </CardContent>
          </Card>
        </div>
      </section>

      {/* SERVICIOS MÉDICOS */}
      <section id="servicios" className="py-24 bg-gradient-to-b from-white to-primary/5 relative">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <Badge variant="outline" className="mb-4 px-4 py-1.5 text-sm font-semibold border-primary/30">
              Nuestros Servicios
            </Badge>
            <h2 className="text-4xl lg:text-5xl font-bold mb-4 bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent">
              Servicios médicos
            </h2>
            <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
              Todo lo que tu mascota necesita, desde prevención hasta cuidado intensivo.
            </p>
          </div>

          <div className="grid md:grid-cols-2 gap-6 lg:gap-8">
            {servicios.map((servicio, index) => {
              const Icon = servicio.icon;
              return (
                <Card
                  key={servicio.title}
                  className="group border-2 border-primary/10 hover:border-primary/40 transition-all duration-300 h-full hover:shadow-2xl hover:-translate-y-1 bg-white/90 backdrop-blur-sm overflow-hidden"
                  style={{
                    animationDelay: `${index * 100}ms`,
                  }}
                >
                  <CardContent className="p-6 lg:p-8 space-y-5 relative">
                    {/* Efecto de brillo al hover */}
                    <div className="absolute inset-0 bg-gradient-to-br from-primary/0 via-primary/0 to-primary/0 group-hover:from-primary/5 group-hover:via-primary/10 group-hover:to-primary/5 transition-all duration-500 -z-0"></div>
                    
                    <div className="flex items-start gap-4 relative z-10">
                      <div className="h-14 w-14 rounded-2xl bg-gradient-to-br from-primary to-primary/70 flex items-center justify-center shadow-lg group-hover:scale-110 group-hover:rotate-3 transition-all duration-300 flex-shrink-0">
                        <Icon className="h-7 w-7 text-white" />
                      </div>
                      <div className="flex-1">
                        <h3 className="font-bold text-xl lg:text-2xl mb-2 group-hover:text-primary transition-colors">
                          {servicio.title}
                        </h3>
                        <p className="text-sm lg:text-base text-muted-foreground leading-relaxed">
                          {servicio.description}
                        </p>
                      </div>
                    </div>
                    
                    {servicio.bullets && (
                      <ul className="space-y-2.5 relative z-10 pt-2 border-t border-primary/10">
                        {servicio.bullets.map((item) => (
                          <li key={item} className="flex items-start gap-2 text-sm text-muted-foreground">
                            <CheckCircle2 className="h-4 w-4 text-primary mt-0.5 flex-shrink-0" />
                            <span>{item}</span>
                          </li>
                        ))}
                      </ul>
                    )}
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </div>
      </section>

      {/* EQUIPOS DE ÚLTIMA TECNOLOGÍA */}
      <section className="bg-gradient-to-b from-primary/5 via-white to-primary/5 py-24 relative">
        <div className="container mx-auto px-4">
          <div className="text-center mb-16">
            <Badge variant="outline" className="mb-4 px-4 py-1.5 text-sm font-semibold border-primary/30">
              Tecnología Avanzada
            </Badge>
            <h2 className="text-4xl lg:text-5xl font-bold mb-4 bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent">
              Equipos de última tecnología
            </h2>
            <p className="text-lg text-muted-foreground max-w-3xl mx-auto">
              Contamos con una infraestructura moderna que respalda cada procedimiento y
              garantiza diagnósticos confiables.
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            {equipos.map((equipo, index) => (
              <Card 
                key={equipo.title} 
                className="group border-2 border-primary/10 hover:border-primary/40 transition-all duration-300 hover:shadow-xl hover:-translate-y-2 bg-white/90 backdrop-blur-sm overflow-hidden"
              >
                <CardContent className="p-6 space-y-3 relative">
                  <div className="absolute top-0 right-0 w-20 h-20 bg-primary/5 rounded-bl-full group-hover:bg-primary/10 transition-colors"></div>
                  <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-primary/20 to-primary/10 flex items-center justify-center mb-3 group-hover:scale-110 transition-transform">
                    <Scan className="h-6 w-6 text-primary" />
                  </div>
                  <h3 className="font-bold text-lg group-hover:text-primary transition-colors relative z-10">
                    {equipo.title}
                  </h3>
                  <p className="text-sm text-muted-foreground leading-relaxed relative z-10">
                    {equipo.description}
                  </p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* CTA INTERMEDIO */}
      <section className="relative py-20 bg-gradient-to-r from-primary via-primary/90 to-secondary text-white text-center overflow-hidden">
        {/* Elementos decorativos */}
        <div className="absolute inset-0 opacity-10">
          <div className="absolute top-10 left-10 w-40 h-40 border-4 border-white/30 rounded-full"></div>
          <div className="absolute bottom-10 right-10 w-32 h-32 border-4 border-white/30 rounded-full"></div>
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-64 h-64 border-4 border-white/20 rounded-full"></div>
        </div>
        
        <div className="container mx-auto px-4 relative z-10">
          <div className="max-w-3xl mx-auto">
            <h2 className="text-4xl lg:text-5xl font-bold mb-6 leading-tight">
              Agenda la salud de tu mascota{' '}
              <span className="relative inline-block">
                hoy mismo<span className="absolute bottom-2 left-0 w-full h-3 bg-white/30 -z-0" />
              </span>
            </h2>
            <p className="mb-8 text-xl opacity-95 leading-relaxed">
              Nuestro equipo está listo para brindarte atención profesional, cercana y humana.
            </p>
            <Button
              size="lg"
              className="bg-white text-primary font-bold hover:bg-gray-100 shadow-2xl hover:shadow-3xl transition-all duration-300 hover:scale-105 px-8 py-6 text-lg group"
              onClick={() => navigate('/agendar-cita')}
            >
              Solicitar Cita Ahora
              <ArrowRight className="ml-2 h-5 w-5 group-hover:translate-x-1 transition-transform" />
            </Button>
          </div>
        </div>
      </section>

      {/* VALORES */}
      <section className="py-24 bg-white relative">
        <div className="container mx-auto px-4 max-w-6xl">
          <div className="text-center mb-16">
            <Badge variant="outline" className="mb-4 px-4 py-1.5 text-sm font-semibold border-primary/30">
              Nuestra Filosofía
            </Badge>
            <h2 className="text-4xl lg:text-5xl font-bold mb-6 bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent">
              Nuestra filosofía y valores
            </h2>
            <p className="text-lg lg:text-xl text-center text-muted-foreground max-w-3xl mx-auto leading-relaxed">
              Creemos que los animales son miembros valiosos de la familia y merecen recibir
              atención médica excepcional. Nos enfocamos en fortalecer el vínculo entre humanos y
              animales a través de la educación, el cuidado y el amor.
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            {valores.map((valor, index) => {
              const Icon = valor.icon;
              return (
                <Card
                  key={valor.title}
                  className="group text-center border-2 border-primary/10 hover:border-primary/40 transition-all duration-300 h-full hover:shadow-xl hover:-translate-y-2 bg-white/90 backdrop-blur-sm"
                >
                  <CardContent className="p-6 lg:p-8 space-y-4 relative overflow-hidden">
                    {/* Efecto de fondo al hover */}
                    <div className="absolute inset-0 bg-gradient-to-br from-primary/0 to-secondary/0 group-hover:from-primary/5 group-hover:to-secondary/5 transition-all duration-500"></div>
                    
                    <div className="h-16 w-16 rounded-2xl bg-gradient-to-br from-primary to-primary/70 mx-auto flex justify-center items-center shadow-lg group-hover:scale-110 group-hover:rotate-3 transition-all duration-300 relative z-10">
                      <Icon className="h-8 w-8 text-white" />
                    </div>
                    <h4 className="font-bold text-lg group-hover:text-primary transition-colors relative z-10">
                      {valor.title}
                    </h4>
                    <p className="text-sm text-muted-foreground leading-relaxed relative z-10">
                      {valor.description}
                    </p>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        </div>
      </section>

      {/* OBJETIVOS ESTRATÉGICOS */}
      <section className="py-24 bg-gradient-to-b from-primary/5 via-white to-primary/5 relative">
        <div className="container mx-auto px-4 max-w-4xl">
          <div className="text-center mb-12">
            <Badge variant="outline" className="mb-4 px-4 py-1.5 text-sm font-semibold border-primary/30">
              Visión Estratégica
            </Badge>
            <h2 className="text-4xl lg:text-5xl font-bold mb-8 bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent">
              Objetivos estratégicos
            </h2>
          </div>
          
          <Card className="border-2 border-primary/10 shadow-xl bg-white/90 backdrop-blur-sm">
            <CardContent className="p-8 lg:p-12">
              <ul className="space-y-5">
                {objetivosEstrategicos.map((objetivo, index) => (
                  <li 
                    key={objetivo} 
                    className="flex gap-4 items-start group hover:bg-primary/5 p-4 rounded-lg transition-all duration-300"
                  >
                    <div className="h-8 w-8 rounded-full bg-gradient-to-br from-primary to-primary/70 flex items-center justify-center flex-shrink-0 shadow-md group-hover:scale-110 transition-transform">
                      <CheckCircle2 className="h-5 w-5 text-white" />
                    </div>
                    <span className="text-base lg:text-lg text-muted-foreground leading-relaxed pt-1 group-hover:text-foreground transition-colors">
                      {objetivo}
                    </span>
                  </li>
                ))}
              </ul>
            </CardContent>
          </Card>
        </div>
      </section>

      {/* CONTACTO */}
      <section id="contacto" className="py-24 bg-white relative overflow-hidden">
        <div className="container mx-auto px-4 grid lg:grid-cols-2 gap-12 items-center">
          <div className="space-y-8 relative z-10">
            <div>
              <Badge variant="outline" className="mb-4 px-4 py-1.5 text-sm font-semibold border-primary/30">
                Contáctanos
              </Badge>
              <h2 className="text-4xl lg:text-5xl font-bold mb-4 bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text text-transparent">
                Contacto y ubicación
              </h2>
              <p className="text-lg text-muted-foreground leading-relaxed">
                Estamos aquí para cuidar de tu mascota y acompañarte en cada etapa de su vida.
              </p>
            </div>

            <div className="space-y-4">
              <Card className="border-2 border-primary/10 hover:border-primary/40 transition-all duration-300 hover:shadow-lg group">
                <CardContent className="p-6 flex gap-4 items-start">
                  <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-primary to-primary/70 flex items-center justify-center flex-shrink-0 shadow-md group-hover:scale-110 transition-transform">
                    <MapPin className="h-6 w-6 text-white" />
                  </div>
                  <div>
                    <h3 className="font-bold text-lg mb-1 group-hover:text-primary transition-colors">Dirección</h3>
                    <p className="text-muted-foreground leading-relaxed">
                      Calle 6 Norte # 14-26, [Ciudad]
                    </p>
                  </div>
                </CardContent>
              </Card>

              <Card className="border-2 border-primary/10 hover:border-primary/40 transition-all duration-300 hover:shadow-lg group">
                <CardContent className="p-6 flex gap-4 items-start">
                  <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-secondary to-secondary/70 flex items-center justify-center flex-shrink-0 shadow-md group-hover:scale-110 transition-transform">
                    <Phone className="h-6 w-6 text-white" />
                  </div>
                  <div>
                    <h3 className="font-bold text-lg mb-1 group-hover:text-primary transition-colors">Teléfono / WhatsApp</h3>
                    <p className="text-muted-foreground leading-relaxed">+57 XXX XXX XXXX</p>
                  </div>
                </CardContent>
              </Card>
            </div>

            <Button
              className="w-full py-6 text-lg shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-105 group"
              size="lg"
              onClick={() => navigate('/agendar-cita')}
            >
              Solicitar Cita
              <ArrowRight className="ml-2 h-5 w-5 group-hover:translate-x-1 transition-transform" />
            </Button>
          </div>

          <Card className="border-2 border-primary/10 shadow-2xl overflow-hidden group hover:shadow-3xl transition-all duration-300">
            <CardContent className="aspect-square bg-gradient-to-br from-primary/20 via-primary/10 to-secondary/20 flex items-center justify-center relative overflow-hidden p-0">
              {/* Patrón decorativo */}
              <div className="absolute inset-0 opacity-20">
                <div className="absolute top-10 left-10 w-32 h-32 border-4 border-primary/30 rounded-full"></div>
                <div className="absolute bottom-10 right-10 w-24 h-24 border-4 border-primary/30 rounded-full"></div>
              </div>
              <div className="relative z-10 text-center p-8">
                <div className="h-24 w-24 rounded-full bg-white/20 backdrop-blur-sm flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform">
                  <MapPin className="h-12 w-12 text-primary" />
                </div>
                <p className="text-sm font-semibold text-muted-foreground">Mapa interactivo</p>
              </div>
            </CardContent>
          </Card>
        </div>
      </section>

      {/* FOOTER */}
      <footer className="relative bg-gradient-to-b from-foreground to-foreground/95 text-white py-16 mt-20 overflow-hidden">
        {/* Elementos decorativos */}
        <div className="absolute inset-0 opacity-5">
          <div className="absolute top-0 left-0 w-64 h-64 border-4 border-white/30 rounded-full"></div>
          <div className="absolute bottom-0 right-0 w-48 h-48 border-4 border-white/30 rounded-full"></div>
        </div>
        
        <div className="container mx-auto px-4 relative z-10">
          <div className="grid md:grid-cols-3 gap-12 mb-12">
            <div className="space-y-4">
              <div className="flex items-center gap-3 mb-4">
                <div className="h-12 w-12 rounded-full bg-gradient-to-br from-primary to-primary/60 flex items-center justify-center">
                  <Dog className="h-6 w-6 text-white" />
                </div>
                <h3 className="font-bold text-xl">Clínica Veterinaria Universitaria Humboldt</h3>
              </div>
              <p className="text-gray-300 leading-relaxed">
                Cuidando vidas, formando profesionales.
              </p>
            </div>

            <div className="space-y-4">
              <h4 className="font-bold text-lg mb-4">Contacto</h4>
              <div className="space-y-3">
                <div className="flex items-center gap-3 group">
                  <Phone className="h-5 w-5 text-primary group-hover:scale-110 transition-transform" />
                  <a href="tel:+573186160630" className="text-gray-300 hover:text-white transition-colors">
                    +573186160630
                  </a>
                </div>
                <div className="flex items-center gap-3 group">
                  <MessageSquare className="h-5 w-5 text-primary group-hover:scale-110 transition-transform" />
                  <a href="mailto:contacto@vetclinic.com" className="text-gray-300 hover:text-white transition-colors">
                    contacto@vetclinic.com
                  </a>
                </div>
              </div>
            </div>

            <div className="space-y-4">
              <h4 className="font-bold text-lg mb-4">Síguenos</h4>
              <div className="flex gap-4">
                <a
                  href="https://www.instagram.com/clinicaveterinariahumboldt/"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="h-10 w-10 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center transition-all duration-300 hover:scale-110 group"
                  aria-label="Facebook"
                >
                  <svg
                    className="h-5 w-5 text-white group-hover:text-primary transition-colors"
                    fill="currentColor"
                    viewBox="0 0 24 24"
                    aria-hidden="true"
                  >
                    <path
                      fillRule="evenodd"
                      d="M22 12c0-5.523-4.477-10-10-10S2 6.477 2 12c0 4.991 3.657 9.128 8.438 9.878v-6.987h-2.54V12h2.54V9.797c0-2.506 1.492-3.89 3.777-3.89 1.094 0 2.238.195 2.238.195v2.46h-1.26c-1.243 0-1.63.771-1.63 1.562V12h2.773l-.443 2.89h-2.33v6.988C18.343 21.128 22 16.991 22 12z"
                      clipRule="evenodd"
                    />
                  </svg>
                </a>
                <a
                  href="https://www.instagram.com/clinicaveterinariahumboldt/"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="h-10 w-10 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center transition-all duration-300 hover:scale-110 group"
                  aria-label="Instagram"
                >
                  <svg
                    className="h-5 w-5 text-white group-hover:text-primary transition-colors"
                    fill="currentColor"
                    viewBox="0 0 24 24"
                    aria-hidden="true"
                  >
                    <path
                      fillRule="evenodd"
                      d="M12.315 2c2.43 0 2.784.013 3.808.06 1.064.049 1.791.218 2.427.465a4.902 4.902 0 011.772 1.153 4.902 4.902 0 011.153 1.772c.247.636.416 1.363.465 2.427.048 1.067.06 1.407.06 4.123v.08c0 2.643-.012 2.987-.06 4.043-.049 1.064-.218 1.791-.465 2.427a4.902 4.902 0 01-1.153 1.772 4.902 4.902 0 01-1.772 1.153c-.636.247-1.363.416-2.427.465-1.067.048-1.407.06-4.123.06h-.08c-2.643 0-2.987-.012-4.043-.06-1.064-.049-1.791-.218-2.427-.465a4.902 4.902 0 01-1.772-1.153 4.902 4.902 0 01-1.153-1.772c-.247-.636-.416-1.363-.465-2.427-.047-1.024-.06-1.379-.06-3.808v-.63c0-2.43.013-2.784.06-3.808.049-1.064.218-1.791.465-2.427a4.902 4.902 0 011.153-1.772A4.902 4.902 0 015.45 2.525c.636-.247 1.363-.416 2.427-.465C8.901 2.013 9.256 2 11.685 2h.63zm-.081 1.802h-.468c-2.456 0-2.784.011-3.807.058-.975.045-1.504.207-1.857.344-.467.182-.8.398-1.15.748-.35.35-.566.683-.748 1.15-.137.353-.3.882-.344 1.857-.047 1.023-.058 1.351-.058 3.807v.468c0 2.456.011 2.784.058 3.807.045.975.207 1.504.344 1.857.182.466.399.8.748 1.15.35.35.683.566 1.15.748.353.137.882.3 1.857.344 1.054.048 1.37.058 4.041.058h.08c2.597 0 2.917-.01 3.96-.058.976-.045 1.505-.207 1.858-.344.466-.182.8-.398 1.15-.748.35-.35.566-.683.748-1.15.137-.353.3-.882.344-1.857.048-1.055.058-1.37.058-4.041v-.08c0-2.597-.01-2.917-.058-3.96-.045-.976-.207-1.505-.344-1.858a3.097 3.097 0 00-.748-1.15 3.098 3.098 0 00-1.15-.748c-.353-.137-.882-.3-1.857-.344-1.023-.047-1.351-.058-3.807-.058zM12 6.865a5.135 5.135 0 110 10.27 5.135 5.135 0 010-10.27zm0 1.802a3.333 3.333 0 100 6.666 3.333 3.333 0 000-6.666zm5.338-3.205a1.2 1.2 0 110 2.4 1.2 1.2 0 010-2.4z"
                      clipRule="evenodd"
                    />
                  </svg>
                </a>
              </div>
            </div>
          </div>

          <div className="border-t border-white/10 pt-8">
            <p className="text-center text-gray-400 text-sm">
              © {new Date().getFullYear()} Clínica Veterinaria Universitaria Humboldt — Todos los
              derechos reservados.
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
}

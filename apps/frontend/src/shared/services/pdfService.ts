import jsPDF from 'jspdf';
import { Consulta, Prescripcion, Paciente, Propietario, Usuario } from '@core/types';

/**
 * Servicio para generar y exportar documentos PDF
 */
export const pdfService = {
  /**
   * Genera un PDF del historial clínico completo de un paciente
   */
  generarHistorialClinico(
    paciente: Paciente,
    propietario: Propietario | null,
    consultas: Consulta[]
  ): void {
    const doc = new jsPDF();
    const pageWidth = doc.internal.pageSize.getWidth();
    const pageHeight = doc.internal.pageSize.getHeight();
    const margin = 20;
    let yPosition = margin;

    // Función helper para agregar nueva página si es necesario
    const checkPageBreak = (requiredSpace: number) => {
      if (yPosition + requiredSpace > pageHeight - margin) {
        doc.addPage();
        yPosition = margin;
      }
    };

    // Encabezado
    doc.setFontSize(20);
    doc.setFont('helvetica', 'bold');
    doc.text('HISTORIAL CLÍNICO', pageWidth / 2, yPosition, { align: 'center' });
    yPosition += 10;

    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    doc.text(`Generado el: ${new Date().toLocaleDateString('es-ES')}`, pageWidth / 2, yPosition, { align: 'center' });
    yPosition += 15;

    // Información del Paciente
    doc.setFontSize(14);
    doc.setFont('helvetica', 'bold');
    doc.text('INFORMACIÓN DEL PACIENTE', margin, yPosition);
    yPosition += 8;

    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    const pacienteInfo = [
      `Nombre: ${paciente.nombre}`,
      `Especie: ${paciente.especie}`,
      paciente.raza ? `Raza: ${paciente.raza}` : null,
      paciente.sexo ? `Sexo: ${paciente.sexo === 'M' ? 'Macho' : 'Hembra'}` : null,
      paciente.edadMeses ? `Edad: ${Math.floor(paciente.edadMeses / 12)} años ${paciente.edadMeses % 12} meses` : null,
      paciente.pesoKg ? `Peso: ${paciente.pesoKg} kg` : null,
    ].filter(Boolean);

    pacienteInfo.forEach((line) => {
      checkPageBreak(7);
      doc.text(line as string, margin + 5, yPosition);
      yPosition += 7;
    });

    // Información del Propietario
    if (propietario) {
      yPosition += 5;
      checkPageBreak(20);
      doc.setFontSize(14);
      doc.setFont('helvetica', 'bold');
      doc.text('INFORMACIÓN DEL PROPIETARIO', margin, yPosition);
      yPosition += 8;

      doc.setFontSize(10);
      doc.setFont('helvetica', 'normal');
      const propietarioInfo = [
        `Nombre: ${propietario.nombre}`,
        propietario.documento ? `Documento: ${propietario.documento}` : null,
        propietario.email ? `Email: ${propietario.email}` : null,
        propietario.telefono ? `Teléfono: ${propietario.telefono}` : null,
      ].filter(Boolean);

      propietarioInfo.forEach((line) => {
        checkPageBreak(7);
        doc.text(line as string, margin + 5, yPosition);
        yPosition += 7;
      });
    }

    // Historial de Consultas
    if (consultas.length > 0) {
      yPosition += 10;
      checkPageBreak(20);
      doc.setFontSize(14);
      doc.setFont('helvetica', 'bold');
      doc.text('HISTORIAL DE CONSULTAS', margin, yPosition);
      yPosition += 10;

      consultas.forEach((consulta, index) => {
        checkPageBreak(50);
        
        // Separador entre consultas
        if (index > 0) {
          doc.setDrawColor(200, 200, 200);
          doc.line(margin, yPosition, pageWidth - margin, yPosition);
          yPosition += 5;
        }

        // Fecha y Profesional
        doc.setFontSize(11);
        doc.setFont('helvetica', 'bold');
        const fecha = new Date(consulta.fecha).toLocaleDateString('es-ES', {
          day: '2-digit',
          month: 'long',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit',
        });
        doc.text(`Consulta del ${fecha}`, margin, yPosition);
        yPosition += 6;

        if (consulta.profesionalNombre) {
          doc.setFontSize(10);
          doc.setFont('helvetica', 'italic');
          doc.text(`Atendido por: ${consulta.profesionalNombre}`, margin, yPosition);
          yPosition += 6;
        }

        // Signos Vitales
        const signosVitales = [
          consulta.frecuenciaCardiaca ? `FC: ${consulta.frecuenciaCardiaca} lpm` : null,
          consulta.frecuenciaRespiratoria ? `FR: ${consulta.frecuenciaRespiratoria} rpm` : null,
          consulta.temperatura ? `Temp: ${consulta.temperatura} °C` : null,
          consulta.pesoKg ? `Peso: ${consulta.pesoKg} kg` : null,
        ].filter(Boolean);

        if (signosVitales.length > 0) {
          doc.setFontSize(10);
          doc.setFont('helvetica', 'bold');
          doc.text('Signos Vitales:', margin, yPosition);
          yPosition += 6;
          doc.setFont('helvetica', 'normal');
          signosVitales.forEach((signo) => {
            doc.text(`  • ${signo}`, margin + 5, yPosition);
            yPosition += 6;
          });
        }

        // Examen Físico
        if (consulta.examenFisico) {
          checkPageBreak(15);
          doc.setFontSize(10);
          doc.setFont('helvetica', 'bold');
          doc.text('Examen Físico:', margin, yPosition);
          yPosition += 6;
          doc.setFont('helvetica', 'normal');
          const examenLines = doc.splitTextToSize(consulta.examenFisico, pageWidth - 2 * margin - 10);
          examenLines.forEach((line: string) => {
            checkPageBreak(6);
            doc.text(line, margin + 5, yPosition);
            yPosition += 6;
          });
        }

        // Diagnóstico
        if (consulta.diagnostico) {
          checkPageBreak(15);
          doc.setFontSize(10);
          doc.setFont('helvetica', 'bold');
          doc.text('Diagnóstico:', margin, yPosition);
          yPosition += 6;
          doc.setFont('helvetica', 'normal');
          const diagnosticoLines = doc.splitTextToSize(consulta.diagnostico, pageWidth - 2 * margin - 10);
          diagnosticoLines.forEach((line: string) => {
            checkPageBreak(6);
            doc.text(line, margin + 5, yPosition);
            yPosition += 6;
          });
        }

        // Tratamiento
        if (consulta.tratamiento) {
          checkPageBreak(15);
          doc.setFontSize(10);
          doc.setFont('helvetica', 'bold');
          doc.text('Tratamiento:', margin, yPosition);
          yPosition += 6;
          doc.setFont('helvetica', 'normal');
          const tratamientoLines = doc.splitTextToSize(consulta.tratamiento, pageWidth - 2 * margin - 10);
          tratamientoLines.forEach((line: string) => {
            checkPageBreak(6);
            doc.text(line, margin + 5, yPosition);
            yPosition += 6;
          });
        }

        // Observaciones
        if (consulta.observaciones) {
          checkPageBreak(15);
          doc.setFontSize(10);
          doc.setFont('helvetica', 'bold');
          doc.text('Observaciones:', margin, yPosition);
          yPosition += 6;
          doc.setFont('helvetica', 'normal');
          const observacionesLines = doc.splitTextToSize(consulta.observaciones, pageWidth - 2 * margin - 10);
          observacionesLines.forEach((line: string) => {
            checkPageBreak(6);
            doc.text(line, margin + 5, yPosition);
            yPosition += 6;
          });
        }

        yPosition += 5;
      });
    } else {
      yPosition += 10;
      doc.setFontSize(10);
      doc.setFont('helvetica', 'italic');
      doc.text('No hay consultas registradas', margin, yPosition);
    }

    // Pie de página
    const totalPages = doc.getNumberOfPages();
    for (let i = 1; i <= totalPages; i++) {
      doc.setPage(i);
      doc.setFontSize(8);
      doc.setFont('helvetica', 'normal');
      doc.text(
        `Página ${i} de ${totalPages}`,
        pageWidth / 2,
        pageHeight - 10,
        { align: 'center' }
      );
    }

    // Descargar PDF
    const fileName = `Historial_Clinico_${paciente.nombre.replace(/\s+/g, '_')}_${new Date().toISOString().split('T')[0]}.pdf`;
    doc.save(fileName);
  },

  /**
   * Genera un PDF de una prescripción médica
   */
  generarPrescripcion(
    prescripcion: Prescripcion,
    consulta: Consulta | null,
    paciente: Paciente | null,
    propietario: Propietario | null,
    profesional: Usuario | null
  ): void {
    const doc = new jsPDF();
    const pageWidth = doc.internal.pageSize.getWidth();
    const pageHeight = doc.internal.pageSize.getHeight();
    const margin = 20;
    let yPosition = margin;

    // Encabezado
    doc.setFontSize(18);
    doc.setFont('helvetica', 'bold');
    doc.text('PRESCRIPCIÓN MÉDICA', pageWidth / 2, yPosition, { align: 'center' });
    yPosition += 10;

    doc.setFontSize(10);
    doc.setFont('helvetica', 'normal');
    const fechaEmision = prescripcion.fechaEmision
      ? new Date(prescripcion.fechaEmision).toLocaleDateString('es-ES')
      : new Date().toLocaleDateString('es-ES');
    doc.text(`Fecha de Emisión: ${fechaEmision}`, pageWidth / 2, yPosition, { align: 'center' });
    yPosition += 15;

    // Información del Paciente
    if (paciente) {
      doc.setFontSize(12);
      doc.setFont('helvetica', 'bold');
      doc.text('PACIENTE', margin, yPosition);
      yPosition += 8;

      doc.setFontSize(10);
      doc.setFont('helvetica', 'normal');
      doc.text(`Nombre: ${paciente.nombre}`, margin, yPosition);
      yPosition += 6;
      doc.text(`Especie: ${paciente.especie}`, margin, yPosition);
      yPosition += 6;
      if (paciente.raza) {
        doc.text(`Raza: ${paciente.raza}`, margin, yPosition);
        yPosition += 6;
      }
      if (paciente.pesoKg) {
        doc.text(`Peso: ${paciente.pesoKg} kg`, margin, yPosition);
        yPosition += 6;
      }
      yPosition += 5;
    }

    // Información del Propietario
    if (propietario) {
      doc.setFontSize(12);
      doc.setFont('helvetica', 'bold');
      doc.text('PROPIETARIO', margin, yPosition);
      yPosition += 8;

      doc.setFontSize(10);
      doc.setFont('helvetica', 'normal');
      doc.text(`Nombre: ${propietario.nombre}`, margin, yPosition);
      yPosition += 6;
      if (propietario.documento) {
        doc.text(`Documento: ${propietario.documento}`, margin, yPosition);
        yPosition += 6;
      }
      yPosition += 5;
    }

    // Información de la Consulta
    if (consulta) {
      doc.setFontSize(12);
      doc.setFont('helvetica', 'bold');
      doc.text('CONSULTA', margin, yPosition);
      yPosition += 8;

      doc.setFontSize(10);
      doc.setFont('helvetica', 'normal');
      const fechaConsulta = new Date(consulta.fecha).toLocaleDateString('es-ES', {
        day: '2-digit',
        month: 'long',
        year: 'numeric',
      });
      doc.text(`Fecha: ${fechaConsulta}`, margin, yPosition);
      yPosition += 6;
      if (consulta.diagnostico) {
        const diagnosticoLines = doc.splitTextToSize(`Diagnóstico: ${consulta.diagnostico}`, pageWidth - 2 * margin);
        diagnosticoLines.forEach((line: string) => {
          doc.text(line, margin, yPosition);
          yPosition += 6;
        });
      }
      yPosition += 5;
    }

    // Medicamentos
    if (prescripcion.items && prescripcion.items.length > 0) {
      doc.setFontSize(12);
      doc.setFont('helvetica', 'bold');
      doc.text('MEDICAMENTOS', margin, yPosition);
      yPosition += 10;

      prescripcion.items.forEach((item, index) => {
        if (yPosition > pageHeight - 60) {
          doc.addPage();
          yPosition = margin;
        }

        doc.setFontSize(10);
        doc.setFont('helvetica', 'bold');
        doc.text(`${index + 1}. ${item.medicamento}`, margin, yPosition);
        yPosition += 6;

        doc.setFont('helvetica', 'normal');
        if (item.presentacion) {
          doc.text(`   Presentación: ${item.presentacion}`, margin, yPosition);
          yPosition += 6;
        }
        doc.text(`   Dosis: ${item.dosis}`, margin, yPosition);
        yPosition += 6;
        doc.text(`   Frecuencia: ${item.frecuencia}`, margin, yPosition);
        yPosition += 6;
        doc.text(`   Duración: ${item.duracionDias} días`, margin, yPosition);
        yPosition += 6;
        if (item.viaAdministracion) {
          doc.text(`   Vía: ${item.viaAdministracion}`, margin, yPosition);
          yPosition += 6;
        }
        if (item.indicaciones) {
          const indicacionesLines = doc.splitTextToSize(`   Indicaciones: ${item.indicaciones}`, pageWidth - 2 * margin - 10);
          indicacionesLines.forEach((line: string) => {
            doc.text(line, margin, yPosition);
            yPosition += 6;
          });
        }
        yPosition += 5;
      });
    }

    // Indicaciones Generales
    if (prescripcion.indicacionesGenerales) {
      if (yPosition > pageHeight - 40) {
        doc.addPage();
        yPosition = margin;
      }
      yPosition += 5;
      doc.setFontSize(12);
      doc.setFont('helvetica', 'bold');
      doc.text('INDICACIONES GENERALES', margin, yPosition);
      yPosition += 8;

      doc.setFontSize(10);
      doc.setFont('helvetica', 'normal');
      const indicacionesLines = doc.splitTextToSize(
        prescripcion.indicacionesGenerales,
        pageWidth - 2 * margin
      );
      indicacionesLines.forEach((line: string) => {
        if (yPosition > pageHeight - 20) {
          doc.addPage();
          yPosition = margin;
        }
        doc.text(line, margin, yPosition);
        yPosition += 6;
      });
    }

    // Firma del Profesional
    if (profesional) {
      if (yPosition > pageHeight - 40) {
        doc.addPage();
        yPosition = margin;
      }
      yPosition += 20;
      doc.setFontSize(10);
      doc.setFont('helvetica', 'normal');
      doc.text('_________________________', margin, yPosition);
      yPosition += 8;
      doc.text(profesional.nombre || 'Veterinario', margin, yPosition);
      yPosition += 6;
      if (profesional.email) {
        doc.setFontSize(9);
        doc.text(profesional.email, margin, yPosition);
      }
    }

    // Pie de página
    const totalPages = doc.getNumberOfPages();
    for (let i = 1; i <= totalPages; i++) {
      doc.setPage(i);
      doc.setFontSize(8);
      doc.setFont('helvetica', 'normal');
      doc.text(
        `Página ${i} de ${totalPages}`,
        pageWidth / 2,
        pageHeight - 10,
        { align: 'center' }
      );
    }

    // Descargar PDF
    const pacienteNombre = paciente?.nombre || 'Paciente';
    const fileName = `Prescripcion_${pacienteNombre.replace(/\s+/g, '_')}_${new Date().toISOString().split('T')[0]}.pdf`;
    doc.save(fileName);
  },
};


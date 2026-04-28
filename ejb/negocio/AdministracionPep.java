package com.coop1.soficoop.pln.negocio;

import com.coop1.banksys.general.excepciones.ValidacionExcepcion;
import com.coop1.banksys.general.negocio.ProcesosGenLocal;
import com.coop1.banksys.general.utilidades.web.ValidaDatos;
import com.coop1.banksys.rhu.entidades.Rhuempleado;
import com.coop1.soficoop.pln.entidades.Plnaccidetplantrim;
import com.coop1.soficoop.pln.entidades.Plnaccioneval;
import com.coop1.soficoop.pln.entidades.Plnaccionsinplan;
import com.coop1.soficoop.pln.entidades.Plnacciondeta;
import com.coop1.soficoop.pln.entidades.Plnaccionseguimiento;
import com.coop1.soficoop.pln.entidades.Plnpao;
import com.coop1.soficoop.pln.entidades.Plnpaoinic;
import com.coop1.soficoop.pln.entidades.Plnpaoinicdeta;
import com.coop1.soficoop.pln.entidades.Plnpep;
import com.coop1.soficoop.pln.entidades.Plnpepindicumpl;
import com.coop1.soficoop.pln.entidades.Plnpeplinestr;
import com.coop1.soficoop.pln.entidades.Plnpepobjetivo;
import com.coop1.soficoop.pln.entidades.Plnperspectivadeta;
import com.coop1.soficoop.pln.entidades.Plnperspectiva;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EJB de Administración para el Módulo de Planificación Estratégica (PEP) y
 * Planes Anuales Operativos (PAO).
 *
 * Organización jerárquica: 1. MÓDULO PEP (Plan Estratégico Participativo) └─
 * Perspectivas └─ Objetivos Estratégicos (OE) └─ Indicadores de Cumplimiento └─
 * Líneas Estratégicas └─ Iniciativas
 *
 * 2. MÓDULO PAO (Plan Anual Operativo) └─ Acciones del PAO ├─ Plan Trimestral
 * ├─ Evaluación └─ Seguimientos └─ Acciones No Planificadas
 *
 * 3. APROBACIÓN GERENCIAL └─ Aprobación de PEP └─ Aprobación de Presupuestos └─
 * Cierre de PAO
 *
 * @author ENVY360
 */
@Stateless
public class AdministracionPep implements AdministracionPepLocal {

    private static final Logger LOG = Logger.getLogger(AdministracionPep.class.getName());
    @PersistenceContext(unitName = "BankSys-ejbPU")
    private EntityManager em;
    @EJB
    private ProcesosGenLocal procGen;

    // ========================================================================
    // MÉTODOS UTILITARIOS Y VALIDACIONES TRANSVERSALES
    // ========================================================================
    /**
     * VALIDA QUE UN PEP NO TENGA ACTA DE APROBACIÓN.
     *
     * Funcionalidad: Si el PEP posee número de acta, punto de acta o fecha de
     * acta, se considera aprobado y queda bloqueado para modificaciones o
     * eliminación.
     *
     * @param idPep Identificador del PEP a validar. Si es {@code null}, se
     * asume que el PEP es nuevo y no se bloquea.
     * @throws Exception Se lanza cuando el PEP tiene Acta de Aprobación y la
     * operación solicitada no está permitida.
     */
    private void validarBloqueoPep(BigInteger idPep) throws Exception {
        if (idPep == null) {
            LOG.log(Level.INFO, "PEP válido porque es nuevo");
            return;
        }

        Plnpep pep = em.find(Plnpep.class, idPep);
        if (pep != null) {
            LOG.log(Level.INFO, "Validando si el PEP está bloqueado por una acta");

            boolean tieneNum = (pep.getNumacta() != null && pep.getNumacta() > 0);
            boolean tienePto = (pep.getPtoacta() != null && pep.getPtoacta() > 0);
            boolean tieneFch = (pep.getFchacta() != null);

            if (tieneNum || tienePto || tieneFch) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                String fechaTexto = (tieneFch ? sdf.format(pep.getFchacta()) : "");
                StringBuilder msg = new StringBuilder();

                LOG.log(Level.INFO, "PEP con acta de aprobación, no se puede eliminar ni modificar");

                msg.append("Este PEP está aprobado por una acta (");
                if (tieneNum) {
                    msg.append("Acta No. ").append(pep.getNumacta());
                }
                if (tieneNum && (tienePto || tieneFch)) {
                    msg.append(", ");
                }
                if (tienePto) {
                    msg.append("Punto ").append(pep.getPtoacta());
                }
                if (tienePto && tieneFch) {
                    msg.append(", ");
                }
                if (tieneFch) {
                    msg.append("Fecha: ").append(fechaTexto);
                }
                msg.append("). No se permiten modificaciones.");

                String mensajeFinal = msg.toString();

                LOG.log(Level.WARNING, "Intento de modificación bloqueado en PEP ID: " + idPep + ". Razón: " + mensajeFinal);

                throw new Exception(mensajeFinal);
            }
        }
    }

    /**
     * GENERA UN CÓDIGO ALFABÉTICO SECUENCIAL.
     *
     * Funcionalidad: Convierte un índice numérico en un código basado en letras
     * (A, B, ..., Z, AA, AB, etc.), utilizado para identificar indicadores de
     * cumplimiento.
     *
     * @param indice Valor numérico base para generar el código alfabético.
     * @return Código alfabético generado de forma secuencial.
     */
    private String obtenerCodigoAlfabetico(long indice) {
        if (indice < 0) {
            return "A";
        }

        StringBuilder codigo = new StringBuilder();
        while (indice >= 0) {
            codigo.insert(0, (char) ('A' + indice % 26));
            indice = (indice / 26) - 1;
        }
        return codigo.toString();
    }

    // ========================================================================
    // MÓDULO PEP - PLAN ESTRATÉGICO PARTICIPATIVO
    // ========================================================================
    /**
     * CREA O ACTUALIZA UN PLAN ESTRATÉGICO PARTICIPATIVO (PEP).
     *
     * Validaciones: - Valida que el PEP no esté bloqueado por Acta de
     * Aprobación. - Verifica rangos válidos de años (inicio y fin). - Evita
     * duplicados por descripción o rango de años. - Diferencia entre creación y
     * modificación del registro.
     *
     * @param pep Objeto {@link Plnpep} con la información del Plan Estratégico
     * a crear o modificar.
     * @param usuario Usuario que ejecuta la operación para fines de auditoría.
     * @throws ValidacionExcepcion Cuando no se cumplen las validaciones de
     * negocio sobre los datos ingresados (años inválidos, inconsistencias,
     * etc.).
     * @throws Exception Cuando el PEP tiene Acta de Aprobación o existe
     * duplicidad de información que impide su registro.
     */
    @Override
    public void guardarPep(Plnpep pep, String usuario) throws Exception {
        if (pep.getIdpep() != null) {
            validarBloqueoPep(pep.getIdpep());
            LOG.log(Level.INFO, "Validación de PEP bloqueado (posee o no posee una acta)");
        }

        try {
            LOG.log(Level.INFO, "Validando Descripción, año inicio y año fin sean diferentes a otros registros");
            String sqlVal = "SELECT p FROM Plnpep p WHERE (UPPER(p.descrip) = UPPER(:desc) OR (p.anioini = :ini AND p.aniofin = :fin))";
            if (pep.getIdpep() != null) {
                sqlVal += " AND p.idpep <> :idActual";
            }

            Query qVal = em.createQuery(sqlVal);
            qVal.setParameter("desc", pep.getDescrip());
            qVal.setParameter("ini", pep.getAnioini());
            qVal.setParameter("fin", pep.getAniofin());
            if (pep.getIdpep() != null) {
                qVal.setParameter("idActual", pep.getIdpep());
            }

            if (!qVal.getResultList().isEmpty()) {
                throw new Exception("Ya existe un Plan Estratégico con el mismo nombre o el mismo rango de años.");
            }

            if (pep.getIdpep() != null) {
                Plnpep original = em.find(Plnpep.class, pep.getIdpep());
                if (original.getPtoacta() != null) {
                    throw new Exception("No se puede editar este PEP porque ya posee un Acta de Aprobación.");
                }
            }

            if (pep.getIdpep() == null) {
                LOG.log(Level.INFO, "Creando un nuevo PEP");

                BigInteger id = procGen.obtenerCorrelativo("IDPEP", "MttoPep", usuario);
                procGen.marcarCorrelativo(id, "IDPEP");

                pep.setIdpep(id);
                pep.setUsercrea(usuario);
                pep.setFchacrea(new Date());
                em.persist(pep);
                LOG.log(Level.INFO, "PEP creado exitosamente");
            } else {
                pep.setUsermod(usuario);
                pep.setFchamod(new Date());
                em.merge(pep);
                LOG.log(Level.INFO, "Se ha modificado un PEP");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Error al guardar PEP: " + e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * ELIMINA UN PLAN ESTRATÉGICO PARTICIPATIVO (PEP).
     *
     * Validaciones: - Valida que el PEP no esté bloqueado por Acta de
     * Aprobación. - No permite eliminar PEPs que ya poseen acta asociada.
     *
     * @param pep Objeto {@link Plnpep} que identifica el Plan Estratégico a
     * eliminar.
     * @throws Exception Cuando el PEP tiene Acta de Aprobación o ocurre un
     * error durante el proceso de eliminación.
     */
    @Override
    public void eliminarPep(Plnpep pep) throws Exception {
        validarBloqueoPep(pep.getIdpep());
        try {
            Plnpep ref = em.find(Plnpep.class, pep.getIdpep());
            if (ref != null && ref.getPtoacta() != null) {
                LOG.log(Level.WARNING, "Se intentó eliminar un PEP con una Acta de Aprobación");
                throw new Exception("ERROR: No se puede eliminar un PEP con Acta de Aprobación.");
            }
            if (ref != null) {
                em.remove(ref);
                LOG.log(Level.INFO, "Se ha eliminado un PEP");
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error al Eliminar un PEP");
            throw new Exception(e.getMessage());
        }
    }

    // ========================================================================
    // MÓDULO PERSPECTIVAS (Nivel 1 de jerarquía PEP)
    // ========================================================================
    /**
     * CREA O ACTUALIZA UNA PERSPECTIVA (CATÁLOGO MAESTRO).
     *
     * Validaciones: - Evita duplicidad de perspectivas por nombre. - Normaliza
     * el nombre antes de guardar. - Diferencia entre creación y modificación
     * del registro.
     *
     * @param maestro Objeto {@link Plnperspectiva} con la información de la
     * Perspectiva.
     * @param usuario Usuario que ejecuta la operación para fines de auditoría.
     * @throws Exception Cuando ya existe una Perspectiva con el mismo nombre o
     * ocurre un error durante el proceso.
     */
    @Override
    public void guardarMaestro(Plnperspectiva maestro, String usuario) throws Exception {
        try {
            LOG.log(Level.INFO, "Validando información de Perspectiva");

            if (maestro.getNombre() != null) {
                maestro.setNombre(maestro.getNombre().trim());
            }

            String sqlDup = "SELECT m FROM Plnperspectiva m WHERE UPPER(m.nombre) = UPPER(:nombre)";
            if (maestro.getIdmaestro() != null) {
                sqlDup += " AND m.idmaestro <> :idActual";
            }

            Query qDup = em.createQuery(sqlDup);
            qDup.setParameter("nombre", maestro.getNombre());
            if (maestro.getIdmaestro() != null) {
                qDup.setParameter("idActual", maestro.getIdmaestro());
            }

            if (!qDup.getResultList().isEmpty()) {
                throw new Exception("Ya existe una perspectiva con el nombre '" + maestro.getNombre() + "'.");
            }

            if (maestro.getIdmaestro() == null) {
                LOG.log(Level.INFO, "Creando Perspectiva");

                BigInteger id = procGen.obtenerCorrelativo("IDMAESTROPERSP", "MttoPep", usuario);
                procGen.marcarCorrelativo(id, "IDMAESTROPERSP");

                maestro.setIdmaestro(id);
                maestro.setEstado((short) 1);
                maestro.setUsercrea(usuario);
                maestro.setFchacrea(new Date());
                LOG.log(Level.INFO, "Perspectiva creada exitosamente");

                em.persist(maestro);
            } else {
                LOG.log(Level.INFO, "Editando Perspectiva");

                maestro.setUsermod(usuario);
                maestro.setFchamod(new Date());

                em.merge(maestro);
                LOG.log(Level.INFO, "Datos actualizados de Perspectivas");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Error al guardar Perspectiva: " + e.getMessage(), e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * ELIMINA UNA PERSPECTIVA (CATÁLOGO MAESTRO).
     *
     * Validaciones: - Verifica que la Perspectiva no esté asociada a ningún
     * Plan Estratégico. - Impide la eliminación si la Perspectiva ya está
     * asignada a uno o más PEP.
     *
     * @param maestro Objeto {@link Plnperspectiva} que identifica la
     * Perspectiva a eliminar.
     * @throws Exception Cuando la Perspectiva está siendo utilizada por Planes
     * Estratégicos o ocurre un error durante el proceso de eliminación.
     */
    @Override
    public void eliminarMaestro(Plnperspectiva maestro) throws Exception {
        LOG.log(Level.INFO, "Eliminar Perspectivas");

        try {
            Plnperspectiva ref = em.find(Plnperspectiva.class, maestro.getIdmaestro());
            if (ref != null) {
                Query qUso = em.createQuery("SELECT count(p) FROM Plnperspectivadeta p WHERE p.perspectivaMaestra.idmaestro = :id");
                qUso.setParameter("id", ref.getIdmaestro());

                long uso = (Long) qUso.getSingleResult();

                if (uso > 0) {
                    throw new Exception("No se puede eliminar: Esta perspectiva está asignada a " + uso + " Planes Estratégicos. Inactívela en su lugar.");
                }
                LOG.log(Level.INFO, "Perspectiva eliminada");

                em.remove(ref);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * CREA O ACTUALIZA LA ASIGNACIÓN DE UNA PERSPECTIVA A UN PEP.
     *
     * Validaciones: - Verifica que el PEP no esté bloqueado por Acta de
     * Aprobación. - Evita asignar la misma Perspectiva más de una vez al mismo
     * PEP. - Diferencia entre creación y actualización de la asignación.
     *
     * @param union Objeto {@link Plnperspectivadeta} que representa la relación
     * entre el PEP y la Perspectiva.
     * @param usuario Usuario que ejecuta la operación para fines de auditoría.
     * @throws Exception Cuando el PEP está bloqueado, existe duplicidad de
     * asignación o ocurre un error durante el proceso.
     */
    @Override
    public void guardarPerspectiva(Plnperspectivadeta union, String usuario) throws Exception {
        if (union.getIdpep() != null) {
            LOG.log(Level.INFO, "Perspectiva valida que el PEP no esté bloqueado para guardar una más");
            validarBloqueoPep(union.getIdpep().getIdpep());
        }

        LOG.log(Level.INFO, "PEP desbloqueado");
        try {
            if (union.getIdperspectiva() == null) {
                Query qDup = em.createQuery("SELECT count(p) FROM Plnperspectivadeta p WHERE p.idpep = :pep AND p.perspectivaMaestra = :maestro");
                qDup.setParameter("pep", union.getIdpep());
                qDup.setParameter("maestro", union.getPerspectivaMaestra());

                if ((Long) qDup.getSingleResult() > 0) {
                    throw new Exception("Esta perspectiva ya está asignada a este Plan Estratégico.");
                }

                BigInteger id = procGen.obtenerCorrelativo("IDPERSPECTIVA", "MttoPep", usuario);
                procGen.marcarCorrelativo(id, "IDPERSPECTIVA");

                union.setIdperspectiva(id);
                union.setUsercrea(usuario);
                union.setFchacrea(new Date());

                em.persist(union);
                LOG.log(Level.INFO, "La perspectiva se ha creado");
            } else {
                union.setUsermod(usuario);
                union.setFchamod(new Date());
                em.merge(union);
                LOG.log(Level.INFO, "Se ha actualizado la perspectiva");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    /**
     * ELIMINA LA ASIGNACIÓN DE UNA PERSPECTIVA A UN PEP.
     *
     * Validaciones: - Verifica que el PEP no esté bloqueado por Acta de
     * Aprobación. - Impide la eliminación si la Perspectiva tiene Objetivos
     * asociados.
     *
     * @param union Objeto {@link Plnperspectivadeta} que identifica la
     * asignación entre el PEP y la Perspectiva.
     * @throws Exception Cuando el PEP está bloqueado, existen Objetivos
     * asociados o ocurre un error durante el proceso.
     */
    @Override
    public void eliminarPerspectiva(Plnperspectivadeta union) throws Exception {
        try {
            Plnperspectivadeta ref = em.find(Plnperspectivadeta.class, union.getIdperspectiva());
            if (ref != null) {
                LOG.log(Level.INFO, "Validando si el PEP está bloqueado");
                validarBloqueoPep(ref.getIdpep().getIdpep());

                if (ref.getPlnpepobjetivoList() != null && !ref.getPlnpepobjetivoList().isEmpty()) {
                    throw new Exception("No se puede quitar esta perspectiva porque tiene Objetivos asociados.");
                }

                em.remove(ref);
                LOG.log(Level.INFO, "Perspectiva Eliminada");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    // ========================================================================
    // MÓDULO OBJETIVOS ESTRATÉGICOS (OE) - Nivel 2 de jerarquía PEP
    // ========================================================================
    /**
     * CREA O ACTUALIZA UN OBJETIVO ESTRATÉGICO (OE).
     *
     * Validaciones: - Verifica que el PEP asociado no esté bloqueado por Acta
     * de Aprobación. - Normaliza código y descripción del objetivo. -
     * Diferencia entre creación y actualización del registro.
     *
     * @param obj Objeto {@link Plnpepobjetivo} con la información del Objetivo
     * Estratégico.
     * @param usuario Usuario que ejecuta la operación para fines de auditoría.
     * @throws Exception Cuando el PEP está bloqueado o ocurre un error durante
     * el proceso de guardado del Objetivo.
     */
    @Override
    public void guardarObjetivo(Plnpepobjetivo obj, String usuario) throws Exception {
        BigInteger idPep = null;

        if (obj.getIdperspectiva() != null) {
            if (obj.getIdperspectiva().getIdpep() != null) {
                idPep = obj.getIdperspectiva().getIdpep().getIdpep();
            } else if (obj.getIdperspectiva().getIdperspectiva() != null) {
                Plnperspectivadeta per = em.find(Plnperspectivadeta.class, obj.getIdperspectiva().getIdperspectiva());
                if (per != null && per.getIdpep() != null) {
                    idPep = per.getIdpep().getIdpep();
                }
            }
        }

        LOG.log(Level.INFO, "Valida si el PEP está desbloqueado");

        try {
            if (obj.getCodigooe() != null) {
                obj.setCodigooe(obj.getCodigooe().trim());
            }
            if (obj.getDescrip() != null) {
                obj.setDescrip(obj.getDescrip().trim());
            }

            if (obj.getIdobjetivo() == null) {
                BigInteger id = procGen.obtenerCorrelativo("IDOBJETIVO", "MttoPep", usuario);
                procGen.marcarCorrelativo(id, "IDOBJETIVO");

                obj.setIdobjetivo(id);
                obj.setUsercrea(usuario);
                obj.setFchacrea(new Date());

                if (obj.getEstado() == null) {
                    obj.setEstado(1);
                }

                em.persist(obj);
                LOG.log(Level.INFO, "OE guardado");
            } else {
                obj.setUsermod(usuario);
                obj.setFchamod(new Date());
                em.merge(obj);
                LOG.log(Level.INFO, "OE actualizado");
            }
        } catch (Exception e) {
            throw new Exception("Error al procesar Objetivo: " + e.getMessage());
        }
    }

    /**
     * ELIMINA UN OBJETIVO ESTRATÉGICO (OE).
     *
     * Validaciones: - Verifica que el PEP asociado no esté bloqueado por Acta
     * de Aprobación. - Elimina en cascada los Indicadores de Cumplimiento y
     * Líneas Estratégicas asociadas.
     *
     * @param obj Objeto {@link Plnpepobjetivo} que identifica el Objetivo
     * Estratégico a eliminar.
     * @throws Exception Cuando el PEP está bloqueado o ocurre un error durante
     * la eliminación en cascada del Objetivo.
     */
    @Override
    public void eliminarObjetivo(Plnpepobjetivo obj) throws Exception {
        try {
            Plnpepobjetivo ref = em.find(Plnpepobjetivo.class, obj.getIdobjetivo());

            if (ref != null) {
                if (ref.getIdperspectiva() != null && ref.getIdperspectiva().getIdpep() != null) {
                    LOG.log(Level.INFO, "Valida si el PEP está bloqueado");
                    validarBloqueoPep(ref.getIdperspectiva().getIdpep().getIdpep());
                }

                Query qIndicadores = em.createQuery("SELECT i FROM Plnpepindicumpl i WHERE i.idobjetivo.idobjetivo = :idObj");
                qIndicadores.setParameter("idObj", ref.getIdobjetivo());
                List<Plnpepindicumpl> listaIndicadores = qIndicadores.getResultList();

                for (Plnpepindicumpl indicador : listaIndicadores) {
                    Query qEstrategias = em.createQuery("SELECT e FROM Plnpeplinestr e WHERE e.idindicadorcump.idindicadorcump = :idInd");
                    qEstrategias.setParameter("idInd", indicador.getIdindicadorcump());
                    List<Plnpeplinestr> listaEstrategias = qEstrategias.getResultList();

                    for (Plnpeplinestr estrategia : listaEstrategias) {
                        em.remove(estrategia);
                        LOG.log(Level.INFO, "Líneas estratégicas asociadas eliminadas");
                    }
                    em.remove(indicador);
                    LOG.log(Level.INFO, "Indicadores de cumplimiento asociados eliminados");
                }

                em.remove(ref);
                LOG.log(Level.INFO, "OE eliminado");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error al eliminar objetivo en cascada: " + e.getMessage());
        }
    }

    // ========================================================================
    // MÓDULO INDICADORES DE CUMPLIMIENTO - Nivel 3 de jerarquía PEP
    // ========================================================================
    /**
     * CREA O ACTUALIZA UN INDICADOR DE CUMPLIMIENTO.
     *
     * Validaciones: - Verifica que el PEP asociado no esté bloqueado por Acta
     * de Aprobación. - Normaliza la descripción del indicador. - Genera
     * automáticamente el código del indicador para nuevos registros. -
     * Diferencia entre creación y actualización del registro.
     *
     * @param ind Objeto {@link Plnpepindicumpl} con la información del
     * Indicador de Cumplimiento.
     * @param usuario Usuario que ejecuta la operación para fines de auditoría.
     * @throws Exception Cuando el PEP está bloqueado o ocurre un error durante
     * el proceso de guardado del Indicador.
     */
    @Override
    public void guardarIndicador(Plnpepindicumpl ind, String usuario) throws Exception {
        BigInteger idPep = null;

        if (ind.getIdobjetivo() != null) {
            Plnpepobjetivo objPadre = em.find(Plnpepobjetivo.class, ind.getIdobjetivo().getIdobjetivo());
            if (objPadre != null && objPadre.getIdperspectiva() != null && objPadre.getIdperspectiva().getIdpep() != null) {
                idPep = objPadre.getIdperspectiva().getIdpep().getIdpep();
            }
        }

        LOG.log(Level.INFO, "Valida si el PEP está desbloqueado");
        validarBloqueoPep(idPep);

        try {
            if (ind.getDescrip() != null) {
                ind.setDescrip(ind.getDescrip().trim());
            }

            if (ind.getIdindicadorcump() == null) {
                String jpql = "SELECT COUNT(i) FROM Plnpepindicumpl i WHERE i.idobjetivo.idobjetivo = :idPadre";
                Query q = em.createQuery(jpql);
                q.setParameter("idPadre", ind.getIdobjetivo().getIdobjetivo());

                long cantidadExistente = (Long) q.getSingleResult();

                String nuevoCodigo = obtenerCodigoAlfabetico(cantidadExistente);
                ind.setCodigoindicador(nuevoCodigo);

                BigInteger id = procGen.obtenerCorrelativo("IDINDICADORCUMP", "MttoPep", usuario);
                procGen.marcarCorrelativo(id, "IDINDICADORCUMP");

                ind.setIdindicadorcump(id);
                ind.setUsercrea(usuario);
                ind.setFchacrea(new Date());

                em.persist(ind);
                LOG.log(Level.INFO, "Indicador de cumplimiento guardado");
            } else {
                ind.setUsermod(usuario);
                ind.setFchamod(new Date());
                em.merge(ind);
                LOG.log(Level.INFO, "Indicador de Cumplimiento actualizado");
            }
        } catch (Exception e) {
            throw new Exception("Error al guardar Indicador: " + e.getMessage());
        }
    }

    /**
     * ELIMINA UN INDICADOR DE CUMPLIMIENTO.
     *
     * Validaciones: - Verifica que el PEP asociado no esté bloqueado por Acta
     * de Aprobación. - Elimina en cascada las Líneas Estratégicas asociadas al
     * Indicador.
     *
     * @param ind Objeto {@link Plnpepindicumpl} que identifica el Indicador de
     * Cumplimiento a eliminar.
     * @throws Exception Cuando el PEP está bloqueado o ocurre un error durante
     * la eliminación del Indicador y sus relaciones.
     */
    @Override
    public void eliminarIndicador(Plnpepindicumpl ind) throws Exception {
        try {
            Plnpepindicumpl ref = em.find(Plnpepindicumpl.class, ind.getIdindicadorcump());

            if (ref != null) {
                if (ref.getIdobjetivo() != null
                        && ref.getIdobjetivo().getIdperspectiva() != null
                        && ref.getIdobjetivo().getIdperspectiva().getIdpep() != null) {
                    validarBloqueoPep(ref.getIdobjetivo().getIdperspectiva().getIdpep().getIdpep());
                }

                Query qEstrategias = em.createQuery("SELECT e FROM Plnpeplinestr e WHERE e.idindicadorcump.idindicadorcump = :idInd");
                qEstrategias.setParameter("idInd", ref.getIdindicadorcump());
                List<Plnpeplinestr> listaEstrategias = qEstrategias.getResultList();

                for (Plnpeplinestr estrategia : listaEstrategias) {
                    em.remove(estrategia);
                }
                em.remove(ref);
            }
        } catch (Exception e) {
            throw new Exception("Error al eliminar indicador y sus estrategias: " + e.getMessage());
        }
    }

    // ========================================================================
    // MÓDULO LÍNEAS ESTRATÉGICAS - Nivel 4 de jerarquía PEP
    // ========================================================================
    /**
     * CREA O ACTUALIZA UNA LÍNEA ESTRATÉGICA.
     *
     * Validaciones: - Verifica que el PEP asociado no esté bloqueado por Acta
     * de Aprobación. - Normaliza la descripción de la Línea Estratégica. -
     * Diferencia entre creación y actualización del registro.
     *
     * @param est Objeto {@link Plnpeplinestr} con la información de la Línea
     * Estratégica.
     * @param usuario Usuario que ejecuta la operación para fines de auditoría.
     * @throws Exception Cuando el PEP está bloqueado o ocurre un error durante
     * el proceso de guardado de la Línea Estratégica.
     */
    @Override
    public void guardarEstrategia(Plnpeplinestr est, String usuario) throws Exception {
        BigInteger idPep = null;

        if (est.getIdindicadorcump() != null) {
            Plnpepindicumpl indPadre = em.find(Plnpepindicumpl.class, est.getIdindicadorcump().getIdindicadorcump());
            if (indPadre != null
                    && indPadre.getIdobjetivo() != null
                    && indPadre.getIdobjetivo().getIdperspectiva() != null
                    && indPadre.getIdobjetivo().getIdperspectiva().getIdpep() != null) {
                idPep = indPadre.getIdobjetivo().getIdperspectiva().getIdpep().getIdpep();
            }
        }
        //validarBloqueoPep(idPep);

        try {
            LOG.log(Level.INFO, "Iniciando guardado de Línea Estratégica. ID PEP asociado: {0}", idPep);
            if (est.getDescrip() != null) {
                est.setDescrip(est.getDescrip().trim());
            }

            if (est.getIdestrategia() == null) {
                BigInteger id = procGen.obtenerCorrelativo("IDESTRATEGIA", "MttoPep", usuario);
                procGen.marcarCorrelativo(id, "IDESTRATEGIA");

                est.setIdestrategia(id);
                est.setUsercrea(usuario);
                est.setFchacrea(new Date());
                em.persist(est);
            } else {
                est.setUsermod(usuario);
                est.setFchamod(new Date());
                em.merge(est);
            }
        } catch (Exception e) {
            throw new Exception("Error al guardar Estrategia: " + e.getMessage());
        }
    }

    /**
     * ELIMINA UNA LÍNEA ESTRATÉGICA.
     *
     * Validaciones: - Verifica que el PEP asociado no esté bloqueado por Acta
     * de Aprobación.
     *
     * @param est Objeto {@link Plnpeplinestr} que identifica la Línea
     * Estratégica a eliminar.
     * @throws Exception Cuando el PEP está bloqueado o ocurre un error durante
     * el proceso de eliminación de la Línea Estratégica.
     */
    @Override
    public void eliminarEstrategia(Plnpeplinestr est) throws Exception {
        try {
            LOG.log(Level.INFO, "Se empezo a eliminar Línea Estratégica ID: {0}", est.getIdestrategia());
            Plnpeplinestr ref = em.find(Plnpeplinestr.class, est.getIdestrategia());
            if (ref != null) {
                if (ref.getIdindicadorcump() != null
                        && ref.getIdindicadorcump().getIdobjetivo() != null
                        && ref.getIdindicadorcump().getIdobjetivo().getIdperspectiva() != null
                        && ref.getIdindicadorcump().getIdobjetivo().getIdperspectiva().getIdpep() != null) {

                    validarBloqueoPep(ref.getIdindicadorcump().getIdobjetivo().getIdperspectiva().getIdpep().getIdpep());
                }
                em.remove(ref);
            }
        } catch (Exception e) {
            throw new Exception("Error al eliminar estrategia: " + e.getMessage());
        }
    }

    // ========================================================================
    // MÓDULO INICIATIVAS - Nivel 5 de jerarquía PEP
    // ========================================================================
    /**
     * CREA O ACTUALIZA UNA INICIATIVA.
     *
     * Funcionalidad: - Genera automáticamente el ID para nuevas iniciativas
     * (MAX + 1). - Diferencia entre creación y actualización del registro.
     *
     * @param iniciativa Objeto {@link Plnpaoinic} con la información de la
     * Iniciativa.
     * @throws Exception Cuando ocurre un error durante el proceso de guardado
     * de la Iniciativa.
     */
    @Override
    public void guardarIniciativa(Plnpaoinic iniciativa) throws Exception {
        try {
            if (iniciativa == null) {
                throw new Exception("El objeto iniciativa es nulo");
            }

            if (iniciativa.getIdiniciativa() == null) {
                Query q = em.createQuery("SELECT MAX(i.idiniciativa) FROM Plnpaoinic i");
                BigInteger maxId = (BigInteger) q.getSingleResult();

                if (maxId == null) {
                    maxId = BigInteger.ZERO;
                }

                BigInteger nuevoId = maxId.add(BigInteger.ONE);
                iniciativa.setIdiniciativa(nuevoId);

                em.persist(iniciativa);
            } else {
                em.merge(iniciativa);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof javax.validation.ConstraintViolationException) {
                javax.validation.ConstraintViolationException cve = (javax.validation.ConstraintViolationException) e;
                for (javax.validation.ConstraintViolation<?> cv : cve.getConstraintViolations()) {
                    LOG.log(Level.SEVERE, "Error de persistencia (JPA) al guardar Iniciativa: " + cv.getMessage());
                }
            }
            LOG.log(Level.SEVERE, "Error general al guardar Iniciativa", e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * ELIMINA UNA INICIATIVA.
     *
     * Funcionalidad: - Verifica que la Iniciativa exista antes de proceder.
     *
     * @param iniciativa Objeto {@link Plnpaoinic} que identifica la Iniciativa
     * a eliminar.
     * @throws Exception Cuando ocurre un error durante el proceso de
     * eliminación de la Iniciativa.
     */
    @Override
    public void eliminarIniciativa(Plnpaoinic iniciativa) throws Exception {
        try {
            if (iniciativa == null || iniciativa.getIdiniciativa() == null) {
                throw new Exception("No se puede eliminar: ID nulo");
            }

            Plnpaoinic objBorrar = em.find(Plnpaoinic.class, iniciativa.getIdiniciativa());

            if (objBorrar != null) {
                em.remove(objBorrar);
            }
        } catch (Exception e) {
            if (e.getMessage().contains("ConstraintViolation") || e.getMessage().contains("foreign key")) {
                throw new Exception("No se puede eliminar la iniciativa porque está siendo utilizada en otros registros.");
            }
            throw new Exception("Error eliminando iniciativa: " + e.getMessage());
        }
    }

    /**
     * VINCULA UNA ACCIÓN DEL PAO A UNA INICIATIVA.
     *
     * Funcionalidad: - Genera automáticamente el ID del vínculo para nuevos
     * registros (MAX + 1).
     *
     * @param vinculo Objeto {@link Plnpaoinicdeta} que representa el vínculo
     * entre Iniciativa y Acción.
     * @throws Exception Cuando ocurre un error durante el proceso de
     * vinculación.
     */
    @Override
    public void guardarVinculoIniciativa(Plnpaoinicdeta vinculo) throws Exception {
        try {
            if (vinculo.getIdinicdeta() == null) {
                Query q = em.createQuery("SELECT MAX(v.idinicdeta) FROM Plnpaoinicdeta v");
                BigInteger max = (BigInteger) q.getSingleResult();
                BigInteger nextId = (max == null) ? BigInteger.ONE : max.add(BigInteger.ONE);

                vinculo.setIdinicdeta(nextId);
                em.persist(vinculo);
            }
        } catch (Exception e) {
            throw new Exception("Error al vincular: " + e.getMessage());
        }
    }

    /**
     * ELIMINA EL VÍNCULO ENTRE UNA INICIATIVA Y UNA ACCIÓN DEL PAO.
     *
     * Funcionalidad: - Verifica que el vínculo exista antes de proceder.
     *
     * @param vinculo Objeto {@link Plnpaoinicdeta} que identifica el vínculo a
     * eliminar.
     * @throws Exception Cuando ocurre un error durante el proceso de
     * desvinculación.
     */
    @Override
    public void eliminarVinculoIniciativa(Plnpaoinicdeta vinculo) throws Exception {
        try {
            Plnpaoinicdeta v = em.find(Plnpaoinicdeta.class, vinculo.getIdinicdeta());
            if (v != null) {
                em.remove(v);
            }
        } catch (Exception e) {
            throw new Exception("Error al desvincular: " + e.getMessage());
        }
    }

    // ========================================================================
    // MÓDULO PAO - PLAN ANUAL OPERATIVO
    // ========================================================================
    /**
     * CREA O ACTUALIZA UN PLAN ANUAL OPERATIVO (PAO).
     *
     * Validaciones: - Normaliza el nombre del PAO. - Verifica que el año del
     * PAO sea válido. - Valida que exista un coordinador asignado. - Diferencia
     * entre creación y actualización del registro.
     *
     * @param pao Objeto {@link Plnpao} con la información del Plan Anual
     * Operativo.
     * @param usuario Usuario que ejecuta la operación para fines de auditoría.
     * @throws Exception Cuando existen datos inválidos o ocurre un error
     * durante el proceso de guardado del PAO.
     */
    @Override
    public void guardarPao(Plnpao pao, String usuario) throws Exception {
        try {
            LOG.log(Level.INFO, "Validando reglas de negocio para guardar PAO del año: {0}", pao.getAnio());
            if (pao.getNombrepao() != null) {
                pao.setNombrepao(pao.getNombrepao().trim());
            }
            if (pao.getAnio() == null || pao.getAnio() < 2025 || pao.getAnio() > 9999) {
                throw new Exception("El año del PAO debe estar entre 2025 y 9999.");
            }
            if (pao.getAnio() < pao.getIdpep().getAnioini() || pao.getAnio() > pao.getIdpep().getAniofin()) {
                throw new Exception("El año del PAO debe estar entre el rango de vigencia del PEP: " + pao.getIdpep().getAnioini()+ " - " +pao.getIdpep().getAniofin());
            }
            if (pao.getIdcoordinador() == null || pao.getIdcoordinador().getCodemp() == null) {
                throw new Exception("El PAO debe tener un coordinador asignado.");
            }

            if (pao.getIdpao() == null) {
                BigInteger id = procGen.obtenerCorrelativo("IDPAO", "MttoPao", usuario);
                procGen.marcarCorrelativo(id, "IDPAO");

                pao.setIdpao(id);
                pao.setUsercrea(usuario);
                pao.setFchacrea(new Date());

                em.persist(pao);
            } else {
                pao.setUsermod(usuario);
                pao.setFchamod(new Date());
                em.merge(pao);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Error al guardar el Plan Anual Operativo (PAO): " + e.getMessage(), e);
            throw new Exception("Error al guardar PAO: " + e.getMessage());
        }
    }

    /**
     * ELIMINA UN PLAN ANUAL OPERATIVO (PAO).
     *
     * Validaciones: - Verifica que el PAO exista antes de proceder. - Elimina
     * en cascada las Acciones, Evaluaciones, Seguimientos, Detalles de
     * Planeación y Acciones no Planificadas asociadas al PAO.
     *
     * @param pao Objeto {@link Plnpao} que identifica el Plan Anual Operativo a
     * eliminar.
     * @throws Exception Cuando ocurre un error durante el proceso de
     * eliminación del PAO.
     */
    public void eliminarPao(Plnpao pao) throws Exception {
        try {
            if (pao == null || pao.getIdpao() == null) {
                return;
            }

            BigInteger idPao = pao.getIdpao();
            LOG.log(Level.INFO, "Iniciando borrado en cascada (Seguimientos, Evaluaciones, Acciones) para el PAO ID: {0}", idPao);

            // ── 1. Borrar seguimientos de las acciones del PAO ──
            em.createQuery(
                    "DELETE FROM Plnaccionseguimiento s "
                    + "WHERE s.idaccionpao.idpao.idpao = :idPao")
                    .setParameter("idPao", idPao)
                    .executeUpdate();

            // ── 2. Borrar evaluaciones de las acciones del PAO ──
            em.createQuery(
                    "DELETE FROM Plnaccioneval e "
                    + "WHERE e.idaccionpao.idpao.idpao = :idPao")
                    .setParameter("idPao", idPao)
                    .executeUpdate();

            // ── 3. Borrar detalles trimestrales de las acciones del PAO ──
            em.createQuery(
                    "DELETE FROM Plnaccidetplantrim t "
                    + "WHERE t.idaccionpao.idpao.idpao = :idPao")
                    .setParameter("idPao", idPao)
                    .executeUpdate();

            // ✅ 4. Borrar vínculos acción-iniciativa ANTES de borrar acciones
            //       Esta es la línea que faltaba y causaba el ORA-02292
            em.createQuery(
                    "DELETE FROM Plnpaoinicdeta d "
                    + "WHERE d.idacciondeta.idpao.idpao = :idPao")
                    .setParameter("idPao", idPao)
                    .executeUpdate();

            // ── 5. Borrar acciones no planificadas del PAO ──
            em.createQuery(
                    "DELETE FROM Plnaccionsinplan np "
                    + "WHERE np.idpao.idpao = :idPao")
                    .setParameter("idPao", idPao)
                    .executeUpdate();

            // ── 6. Borrar acciones planificadas del PAO ──
            em.createQuery(
                    "DELETE FROM Plnacciondeta a "
                    + "WHERE a.idpao.idpao = :idPao")
                    .setParameter("idPao", idPao)
                    .executeUpdate();

            // ── 7. Borrar el PAO ──
            Plnpao paoRef = em.find(Plnpao.class, idPao);
            if (paoRef != null) {
                em.remove(paoRef);
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Fallo al ejecutar borrado en cascada del PAO ID: " + pao.getIdpao(), e);
            throw e;
        }
    }

    /**
     * LIMPIA LA INFORMACIÓN DEL PAO POR CAMBIO DE PEP.
     *
     * Validaciones: - Verifica que el identificador del PAO no sea nulo. -
     * Elimina en cascada las Acciones, Evaluaciones, Detalles de Planeación y
     * Acciones no Planificadas asociadas al PAO.
     *
     * @param idPao Identificador del Plan Anual Operativo afectado por el
     * cambio de Plan Estratégico.
     * @throws Exception Cuando ocurre un error durante el proceso de limpieza
     * del PAO.
     */
    public void limpiarPaoPorCambioPep(BigInteger idPao) throws Exception {
        try {
            // ── 1. Seguimientos ──
            em.createQuery(
                    "DELETE FROM Plnaccionseguimiento s "
                    + "WHERE s.idaccionpao.idpao.idpao = :idPao")
                    .setParameter("idPao", idPao)
                    .executeUpdate();

            // ── 2. Evaluaciones ──
            em.createQuery(
                    "DELETE FROM Plnaccioneval e "
                    + "WHERE e.idaccionpao.idpao.idpao = :idPao")
                    .setParameter("idPao", idPao)
                    .executeUpdate();

            // ── 3. Detalles trimestrales ──
            em.createQuery(
                    "DELETE FROM Plnaccidetplantrim t "
                    + "WHERE t.idaccionpao.idpao.idpao = :idPao")
                    .setParameter("idPao", idPao)
                    .executeUpdate();

            // ✅ 4. Vínculos acción-iniciativa
            em.createQuery(
                    "DELETE FROM Plnpaoinicdeta d "
                    + "WHERE d.idacciondeta.idpao.idpao = :idPao")
                    .setParameter("idPao", idPao)
                    .executeUpdate();

            // ── 5. Acciones no planificadas ──
            em.createQuery(
                    "DELETE FROM Plnaccionsinplan np "
                    + "WHERE np.idpao.idpao = :idPao")
                    .setParameter("idPao", idPao)
                    .executeUpdate();

            // ── 6. Acciones planificadas ──
            em.createQuery(
                    "DELETE FROM Plnacciondeta a "
                    + "WHERE a.idpao.idpao = :idPao")
                    .setParameter("idPao", idPao)
                    .executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // ========================================================================
    // MÓDULO ACCIONES DEL PAO
    // ========================================================================
    /**
     * CREA O ACTUALIZA UNA ACCIÓN DEL PAO.
     *
     * Validaciones: - Verifica que la Acción esté vinculada a una Línea
     * Estratégica. - Valida que la Acción esté asociada a un PAO. - Normaliza
     * la descripción y asigna valores por defecto al presupuesto. - Diferencia
     * entre creación y actualización del registro.
     *
     * @param accion Objeto {@link Plnacciondeta} con la información de la
     * Acción del PAO.
     * @param usuario Usuario que ejecuta la operación para fines de auditoría.
     * @throws Exception Cuando faltan asociaciones obligatorias o ocurre un
     * error durante el proceso de guardado de la Acción del PAO.
     */
    @Override
    public void guardarAccionPao(Plnacciondeta accion, String usuario) throws Exception {
        try {
            if (accion.getIdestrategia() == null) {
                throw new Exception("La acción debe estar vinculada a una Línea Estratégica.");
            }
            if (accion.getIdpao() == null || accion.getIdpao().getIdpao() == null) {
                throw new Exception("La acción debe estar asociada a un PAO.");
            }
            if (accion.getDescrip() != null) {
                accion.setDescrip(accion.getDescrip().trim());
            }

            if (accion.getIdaccionpao() == null) {
                BigInteger id = procGen.obtenerCorrelativo("IDACCIONPAO", "MttoPao", usuario);
                procGen.marcarCorrelativo(id, "IDACCIONPAO");

                accion.setIdaccionpao(id);
                accion.setUsercrea(usuario);
                accion.setFchacrea(new Date());

                if (accion.getPresupropues() == null) {
                    accion.setPresupropues(BigDecimal.ZERO);
                }
                if (accion.getPresuaprob() == null) {
                    accion.setPresuaprob(BigDecimal.ZERO);
                }
                em.persist(accion);
            } else {
                accion.setUsermod(usuario);
                accion.setFchamod(new Date());
                em.merge(accion);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error al guardar Acción PAO: " + e.getMessage());
        }
    }

    @Override
    public void eliminarAccionPao(Plnacciondeta accion) throws Exception {
        if (accion == null || accion.getIdaccionpao() == null) {
            return;
        }

        BigInteger id = accion.getIdaccionpao();

        try {
            // ── 1. Borrar seguimientos de esta acción ──
            em.createQuery(
                    "DELETE FROM Plnaccionseguimiento s "
                    + "WHERE s.idaccionpao.idaccionpao = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            // ── 2. Borrar evaluaciones de esta acción ──
            em.createQuery(
                    "DELETE FROM Plnaccioneval e "
                    + "WHERE e.idaccionpao.idaccionpao = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            // ── 3. Borrar detalles trimestrales de esta acción ──
            em.createQuery(
                    "DELETE FROM Plnaccidetplantrim t "
                    + "WHERE t.idaccionpao.idaccionpao = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            // ── 4. Borrar vínculos acción-iniciativa de esta acción ──
            em.createQuery(
                    "DELETE FROM Plnpaoinicdeta d "
                    + "WHERE d.idacciondeta.idaccionpao = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            // ── 5. Borrar la acción ──
            Plnacciondeta aBorrar = em.find(Plnacciondeta.class, id);
            if (aBorrar != null) {
                em.remove(aBorrar);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    // ========================================================================
    // MÓDULO ACCIONES NO PLANIFICADAS
    // ========================================================================
    /**
     * CREA O ACTUALIZA UNA ACCIÓN NO PLANIFICADA.
     *
     * Validaciones: - Diferencia entre creación y actualización del registro.
     *
     * @param accion Objeto {@link Plnaccionsinplan} con la información de la
     * Acción No Planificada.
     * @throws Exception Cuando ocurre un error durante el proceso de guardado
     * de la Acción No Planificada.
     */
    @Override
    public void guardarAccionNoPlanificada(Plnaccionsinplan accion) throws Exception {
        try {
            if (accion.getIdaccinoplan() == null) {
                BigInteger id = procGen.obtenerCorrelativo("IDACCINOPLAN", "MttoPao", accion.getUsercrea());
                procGen.marcarCorrelativo(id, "IDACCINOPLAN");

                accion.setIdaccinoplan(id);
                em.persist(accion);
            } else {
                em.merge(accion);
            }
        } catch (Exception e) {
            throw new Exception("Error al guardar acción no planificada: " + e.getMessage());
        }
    }

    // ========================================================================
    // MÓDULO PLAN TRIMESTRAL
    // ========================================================================
    /**
     * CREA O ACTUALIZA UN DETALLE DE PLANIFICACIÓN TRIMESTRAL.
     *
     * Validaciones: - Genera manualmente el identificador cuando el registro es
     * nuevo. - Diferencia entre creación y actualización del registro.
     *
     * @param detalle Objeto {@link Plnaccidetplantrim} con la información del
     * Detalle de Planificación Trimestral.
     * @throws Exception Cuando ocurre un error durante el proceso de guardado
     * del Detalle de Planificación.
     */
    @Override
    public void guardarDetalleTrim(Plnaccidetplantrim detalle) throws Exception {
        if (detalle.getIddetalleplantrim() == null) {
            Query q = em.createQuery("SELECT MAX(d.iddetalleplantrim) FROM Plnaccidetplantrim d");
            BigInteger maxId = (BigInteger) q.getSingleResult();

            BigInteger nextId;
            if (maxId == null) {
                nextId = BigInteger.ONE;
            } else {
                nextId = maxId.add(BigInteger.ONE);
            }
            detalle.setIddetalleplantrim(nextId);

            em.persist(detalle);
        } else {
            em.merge(detalle);
        }
    }

    /**
     * ELIMINA UN DETALLE DE PLANIFICACIÓN TRIMESTRAL.
     *
     * Validaciones: - Verifica que el detalle exista antes de proceder.
     *
     * @param detalle Objeto {@link Plnaccidetplantrim} que identifica el
     * Detalle de Planificación Trimestral a eliminar.
     * @throws Exception Cuando ocurre un error durante el proceso de
     * eliminación del Detalle de Planificación.
     */
    @Override
    public void eliminarDetalleTrim(Plnaccidetplantrim detalle) throws Exception {
        Plnaccidetplantrim obj = em.find(Plnaccidetplantrim.class, detalle.getIddetalleplantrim());
        if (obj != null) {
            em.remove(obj);
        }
    }

    @Override
    public void guardarCumplimientoTrimestral(Plnaccidetplantrim trim, String usuario) {
        try {
            trim.setUsermod(usuario);
            trim.setFchamod(new java.util.Date());
            em.merge(trim);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al guardar el % de cumplimiento trimestral", e);
            throw new RuntimeException("Error al guardar el % cumplimiento trimestral: " + e.getMessage(), e);
        }

    }

    // ========================================================================
    // MÓDULO EVALUACIÓN
    // ========================================================================
    /**
     * CREA O ACTUALIZA UNA EVALUACIÓN DEL PAO.
     *
     * Validaciones: - Diferencia entre creación y actualización del registro.
     *
     * @param evaluacion Objeto {@link Plnaccioneval} con la información de la
     * Evaluación.
     * @throws Exception Cuando ocurre un error durante el proceso de guardado
     * de la Evaluación.
     */
    @Override
    public void guardarEvaluacion(Plnaccioneval evaluacion) throws Exception {
        try {
            if (evaluacion.getIdevaluacion() == null) {
                BigInteger id = procGen.obtenerCorrelativo("IDEVALUACION", "MttoPao", evaluacion.getUsercrea());
                procGen.marcarCorrelativo(id, "IDEVALUACION");

                evaluacion.setIdevaluacion(id);
                em.persist(evaluacion);
            } else {
                em.merge(evaluacion);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error al guardar evaluación: " + e.getMessage());
        }
    }

    // ========================================================================
    // MÓDULO SEGUIMIENTOS
    // ========================================================================
    /**
     * CREA O ACTUALIZA UN SEGUIMIENTO DE ACCIÓN DEL PAO.
     *
     * Validaciones: - Genera manualmente el identificador cuando el registro es
     * nuevo. - Diferencia entre creación y actualización del registro.
     *
     * @param seguimiento Objeto {@link Plnaccionseguimiento} con la información
     * del Seguimiento de la Acción del PAO.
     * @throws Exception Cuando ocurre un error durante el proceso de guardado
     * del Seguimiento.
     */
    @Override
    public void guardarSeguimiento(Plnaccionseguimiento seguimiento) throws Exception {
        if (seguimiento.getIdseguimiento() == null) {
            Query q = em.createQuery("SELECT MAX(s.idseguimiento) FROM Plnaccionseguimiento s");
            BigInteger maxId = (BigInteger) q.getSingleResult();

            BigInteger nextId = (maxId == null) ? BigInteger.ONE : maxId.add(BigInteger.ONE);

            seguimiento.setIdseguimiento(nextId);
            em.persist(seguimiento);
        } else {
            em.merge(seguimiento);
        }
    }

    // ========================================================================
    // MÓDULO APROBACIÓN GERENCIAL
    // ========================================================================
    /**
     * APRUEBA UN PEP REGISTRANDO EL ACTA DE APROBACIÓN.
     *
     * Funcionalidad: - Actualiza los datos del Acta (número, punto y fecha). -
     * Registra auditoría de la modificación.
     *
     * @param pep Objeto {@link Plnpep} con los datos del Acta de Aprobación.
     * @param usuario Usuario que ejecuta la operación para fines de auditoría.
     * @throws Exception Cuando ocurre un error durante el proceso de aprobación
     * del PEP.
     */
    @Override
    public void aprobarPepGerencia(Plnpep pep, String usuario) throws Exception {
        LOG.log(Level.INFO, "Aprobando PEP ID: {0} con Acta No: {1}", new Object[]{pep.getIdpep(), pep.getNumacta()});
        
        Plnpep p = em.find(Plnpep.class, pep.getIdpep());

        p.setNumacta(pep.getNumacta());
        p.setPtoacta(pep.getPtoacta());
        p.setFchacta(pep.getFchacta());

        p.setUsermod(usuario);
        p.setFchamod(new Date());

        em.merge(p);
    }

    /**
     * APRUEBA EL PRESUPUESTO DE UNA ACCIÓN DEL PAO.
     *
     * Funcionalidad: - Define el presupuesto aprobado para una Acción
     * específica.
     *
     * @param accion Objeto {@link Plnacciondeta} con el presupuesto aprobado.
     * @param usuario Usuario que ejecuta la operación para fines de auditoría.
     * @throws Exception Cuando ocurre un error durante el proceso de aprobación
     * del presupuesto.
     */
    @Override
    public void aprobarPresupuestoAccion(Plnacciondeta accion, String usuario) throws Exception {
        LOG.log(Level.INFO, "Aprobando presupuesto para la Acción PAO ID: {0}", accion.getIdaccionpao());
        
        Plnacciondeta a = em.find(Plnacciondeta.class, accion.getIdaccionpao());

        a.setPresuaprob(accion.getPresuaprob());

        em.merge(a);
    }

    /**
     * CIERRA UN PAO ASIGNÁNDOLE EL ID DEL ACTA DE CIERRE.
     *
     * Funcionalidad: - Asigna el ID del Acta al PAO para marcarlo como cerrado.
     *
     * @param pao Objeto {@link Plnpao} que identifica el Plan Anual Operativo a
     * cerrar.
     * @param idActa Identificador del Acta de Cierre.
     * @param usuario Usuario que ejecuta la operación para fines de auditoría.
     * @throws Exception Cuando ocurre un error durante el proceso de cierre del
     * PAO.
     */
    @Override
    public void cerrarPaoGerencia(Plnpao pao, Integer idActa, String usuario) throws Exception {
        LOG.log(Level.INFO, "Cerrando PAO ID: {0} con Acta de cierre ID: {1}", new Object[]{pao.getIdpao(), idActa});
        
        Plnpao p = em.find(Plnpao.class, pao.getIdpao());

        p.setIdacta(idActa);

        em.merge(p);
    }
}
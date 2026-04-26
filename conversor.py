import csv
import json

def valor_booleano(valor):
    return str(valor).strip().upper() in ["YA", "X", "SI", "TRUE"]

def convertir_csv_a_json(ruta_csv, ruta_json):
    with open(ruta_csv, mode='r', encoding='utf-8') as archivo:
        reader = list(csv.reader(archivo))

        filas = reader[3:]  # saltar filas 1,2,3

        resultado = []

        for fila in filas:
            if not fila or all(c.strip() == "" for c in fila):
                continue

            try:
                data = {
                    "id": fila[2],

                    "datos_personales": {
                        "foto": fila[3],
                        "nombre_completo": fila[4],
                        "numero_documento": fila[5],
                        "fecha_nacimiento": fila[6],
                        "genero": fila[7],
                        "direccion": fila[8],
                        "telefono": fila[9]
                    },

                    "contacto_emergencia": {
                        "nombre": fila[10],
                        "parentesco": fila[11],
                        "telefono": fila[12]
                    },

                    "datos_medicos": {
                        "tipo_sangre": fila[13],
                        "eps": fila[14],
                        "enfermedades": fila[15],
                        "alergias": fila[16],
                        "discapacidad": fila[17]
                    },

                    "carnet_fisico": valor_booleano(fila[21]) if len(fila) > 21 else False,
                    "activo": valor_booleano(fila[22]) if len(fila) > 22 else False
                }

                resultado.append(data)

            except Exception as e:
                print("❌ Error en fila:", fila)
                print(e)

        with open(ruta_json, mode='w', encoding='utf-8') as salida:
            json.dump(resultado, salida, indent=2, ensure_ascii=False)

        print("✅ JSON generado correctamente")


if __name__ == "__main__":
    convertir_csv_a_json("datos.csv", "datos.json")
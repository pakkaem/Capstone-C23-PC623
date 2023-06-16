import cv2
import numpy as np
import datetime
import os
import subprocess as sp

# Inisialisasi video streaming
cap = cv2.VideoCapture('https://cctvjss.jogjakota.go.id/atcs/ATCS_gondomanan.stream/chunklist_w102839609.m3u8') 

# Membatasi label tertentu
target_labels = ['mobil', 'motor', 'sepeda']

# Daftar warna berdasarkan label
color_dict = {'mobil': (0, 0, 255),  # Merah
              'motor': (255, 0, 0),  # Biru
              'sepeda': (0, 255, 0)}  # Hijau

# def save_detected_image(frame, boxes, indices, labels, class_ids, confidences):
    # Menggambar kotak dan label untuk objek yang terdeteksi
    # if len(indices) > 0:
    #     # Mendapatkan waktu saat ini
    #     current_time = datetime.datetime.now()
    #     year = current_time.strftime("%Y")
    #     month = current_time.strftime("%B").lower()
    #     day = current_time.strftime("%d")
    #     clock = current_time.strftime("%H-%M-%S")
        
    #     # Membuat direktori jika belum ada
    #     save_dir = os.path.join('./img', labels[class_ids[indices[0]]], year, month, day)
    #     os.makedirs(save_dir, exist_ok=True)
        
    #     for i in indices.flatten():
    #         x, y, width, height = boxes[i]
    #         label = labels[class_ids[i]]
            
    #         # Simpan gambar dengan cropping sesuai bounding box dan nama yang unik
    #         cropped_image = frame[y:y+height, x:x+width]
            
    #         # Menentukan apakah objek melewati garis atau tidak
    #         if y + height > line_y:
    #             file_name = f'{clock}.jpg'
    #             file_path = os.path.join(save_dir, file_name)
    #             cv2.imwrite(file_path, cropped_image)

# Path ke file konfigurasi YOLOv3
# config_path = './model/yolov3.cfg'
config_path = './model/yolov3-tiny.cfg'
# Path ke file bobot YOLOv3
# weights_path = './model/yolov3.weights'
weights_path = './model/yolov3-tiny.weights'
# Path ke file daftar label yang diketahui oleh YOLOv3
labels_path = './model/coco.names'

# Load daftar label
with open(labels_path, 'r') as f:
    labels = f.read().splitlines()

# Load model YOLOv3 menggunakan OpenCV
net = cv2.dnn.readNetFromDarknet(config_path, weights_path)

# Menggunakan CPU untuk komputasi
net.setPreferableBackend(cv2.dnn.DNN_BACKEND_OPENCV)
net.setPreferableTarget(cv2.dnn.DNN_TARGET_CPU)

# Garis untuk deteksi
line_y = 300

# Dimensi video capture
width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))

ffmpeg_path = '/usr/bin/ffmpeg'

# Membuat FFmpeg pipe
rtsp_url = "rtsp://127.0.0.1:8554/stream"

command = [ffmpeg_path,
           '-y',
           '-f', 'rawvideo',
           '-vcodec', 'rawvideo',
           '-s', '{}x{}'.format(width, height),
           '-pix_fmt', 'bgr24',
           '-r', '30',
           '-i', '-',
           '-c:v', 'libx264',
           '-pix_fmt', 'yuv420p',
           '-preset', 'ultrafast',
           '-tune', 'zerolatency',
           '-b:v', '500k',
           '-f', 'rtsp',
           rtsp_url]


pipe = sp.Popen(command, stdin=sp.PIPE)

while True:
    # Baca setiap frame dari video streaming
    ret, frame = cap.read()
    if not ret:
        break

    # Mendapatkan daftar nama layer output
    layer_outputs = net.getUnconnectedOutLayersNames()

    # Menjalankan forward pass pada frame untuk mendeteksi objek
    blob = cv2.dnn.blobFromImage(frame, 1/255.0, (416, 416), swapRB=True, crop=False)
    net.setInput(blob)
    outputs = net.forward(layer_outputs)

    # Mendapatkan informasi deteksi objek dari output
    boxes = []
    confidences = []
    class_ids = []
    for output in outputs:
        for detection in output:
            scores = detection[5:]
            class_id = np.argmax(scores)
            confidence = scores[class_id]
            if confidence > 0.8 and labels[class_id] in target_labels:
                center_x = int(detection[0] * frame.shape[1])
                center_y = int(detection[1] * frame.shape[0])
                width = int(detection[2] * frame.shape[1])
                height = int(detection[3] * frame.shape[0])
                x = int(center_x - width / 2)
                y = int(center_y - height / 2)
                boxes.append([x, y, width, height])
                confidences.append(float(confidence))
                class_ids.append(class_id)

    # Menggunakan Non-Maximum Suppression untuk mendapatkan deteksi yang lebih baik
    indices = cv2.dnn.NMSBoxes(boxes, confidences, 0.5, 0.5)

    # Menggambar garis deteksi
    # cv2.line(frame, (300, 400), (frame.shape[1], 600), (0, 255, 255), 2)

    # Memanggil fungsi save_detected_image untuk menyimpan gambar deteksi jika melewati garis
    # save_detected_image(frame, boxes, indices, labels, class_ids, confidences)


    # Menggambar kotak dan label untuk objek yang terdeteksi
    if len(indices) > 0:
        for i in indices.flatten():
            x, y, width, height = boxes[i]
            label = labels[class_ids[i]]
            confidence = confidences[i]
            color = color_dict.get(label)
            cv2.rectangle(frame, (x, y), (x + width, y + height), color, 2)
            cv2.putText(frame, '{}: {:.2f}'.format(label, confidence), (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)

    # Menampilkan frame dengan objek terdeteksi
    # cv2.imshow('Object Detection', frame)

    # Menyalurkan output ke FFMPEG
    pipe.stdin.write(frame.tostring())
    
    # Menghentikan loop jika tombol 'q' ditekan
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# Membebaskan sumber daya
cap.release()
pipe.stdin.close()
pipe.wait()
cv2.destroyAllWindows()